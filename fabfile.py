import sys
import re
import tempfile
import random
import couchdb
import pystache
import numpy as np
import matplotlib.cm as cm
import matplotlib.pyplot as plt

from fabric.api import *
from fabric.state import output
from fabric.contrib.console import *
from fabric.exceptions import CommandTimeout

env.hosts = ["127.0.0.1"]
env.password = "surf10"
env.deploy_path = "/tmp/deploy"

env.renderer = pystache.Renderer(search_dirs="templates")
env.server = couchdb.Server()
env.rossan = None

try:
    env.rossan = env.server["rossan"]
except couchdb.ResourceNotFound:
    env.rossan = env.server.create("rossan")


def render(name, context):
    rendered = tempfile.TemporaryFile("w+")
    rendered.write(env.renderer.render_name(name, context))
    rendered.seek(0)
    return rendered


def next_number_for(type):
    numbers = list(env.rossan.view("next/id", reduce=True, group=True)[type])
    if len(numbers):
        return numbers[0].value
    return 1


class Doc(dict):
    def __init__(self, **elements):
        if "number" not in elements:
            elements.update(number=next_number_for(elements["type"]))
        dict.__init__(self, **elements)

        self._id = "%(type)s-%(number)d" % self

    def update(self, other, **kwargs):
        if isinstance(other, (dict, Doc, couchdb.client.Document)):
            other = dict(other)
            if "_id" in other: del other["_id"]
            if "_rev" in other: del other["_rev"]
            if "type" in other: del other["type"]
        dict.update(self, other, **kwargs)

    def __getattr__(self, field):
        if field in self:
            return self[field]
        return self.__dict__[field]

    def __setattr__(self, field, value):
        self[field] = value

    def __repr__(self):
        return repr(dict(self))


def random_midlets(emulation, **filters):
    midlet_classes = emulation.midlet_classes
    for key, value in filters.items():
        midlet_classes = [t for t in midlet_classes if t[key] == value]

    midlets_number = random.randint(1, emulation.max_sensors_in_spot)
    midlet_classes = random.sample(midlet_classes, midlets_number)

    midlets = []
    for i in range(midlets_number):
        midlet = Doc(number=i, **filters)
        midlet.update(midlet_classes[i])
        midlets.append(midlet)

    return midlets


def random_spot(emulation, number, **filters):
    spot = Doc(number=number, **filters)
    spot.version = "1.0.0"
    spot.vendor = "DCC-UFRJ"
    spot.name = "%(_id)s" % spot
    spot.path = "%(_id)s" % spot
    spot.build_file = "%(path)s/build.xml" % spot
    spot.manifest_file = "%(path)s/resources/META-INF/manifest.mf" % spot
    spot.position = random.randint(0, emulation.area)
    spot.range = emulation.range
    spot.interval = emulation.interval
    spot.behavior = emulation.behavior
    spot.midlets = random_midlets(emulation, **filters)
    return spot


def random_emulation(config):
    emulation = Doc(type="emulation")
    emulation.update(config)

    emulation.seed = prompt("Whats the seed?", default=emulation.seed, validate=int)
    if emulation.seed < 0:
        emulation.seed = random.randint(0, sys.maxint)
    random.seed(emulation.seed)

    emulation.behavior = prompt("Whats the behavior?", default=emulation.behavior, validate=int)
    if emulation.behavior < 0:
        emulation.behavior = random.sample(emulation.behaviors, 1)[0]

    emulation.virtual_spots = []

    for i in range(emulation.basestation_midlets):
        spot = random_spot(emulation, i, type="basestation")
        emulation.virtual_spots.append(spot)

    for i in range(emulation.sensor_midlets):
        spot = random_spot(emulation, i, type="sensor")
        emulation.virtual_spots.append(spot)

    return emulation


def clean():
    local("ant clean > /dev/null")
    run("rm /tmp/deploy -rf")


@task
def deploy():
    clean()

    save_emulation = prompt("Save emulation?", default=False, validate=bool)
    config_name = prompt("Configuration name:", default="config-1", validate=str)
    
    config = env.rossan[config_name]
    emulation = random_emulation(config)

    with lcd(".."):
        local("tar czf repo.tgz Rossan/ --exclude=\"\.*\"")
        local("mv repo.tgz Rossan/")
    
    run("mkdir /tmp/deploy -p")
    with cd("/tmp/deploy"):
        emulation_xml = render("emulation", emulation) 
        put(emulation_xml, "emulation.xml")
        emulation_xml.close()

        put("repo.tgz", "repo.tgz")
        run("tar xzf repo.tgz")
        run("mv Rossan repo")
        run("cp repo/build.xml .")

        for spot in emulation.virtual_spots:
            run("cp repo %(path)s -r" % spot)
            
            manifest = render("manifest", spot)
            put(manifest, "%(manifest_file)s" % spot)
            manifest.close()

    env.emulation = emulation

    if save_emulation:
        print "New emulation: %(_id)s" % emulation
        env.rossan[emulation._id] = emulation

@task
def emulate():
    with cd("/tmp/deploy"):
        out = tempfile.TemporaryFile("w+")
        try:
            run("""LD_LIBRARY_PATH=/usr/lib/jvm/default-java/jre/lib/i386/client \
                   DISPLAY=:0.0 \
                   ant solarium -Dconfig.file=emulation.xml | tee results.txt""",
                timeout=env.emulation.time, stdout=out)
        except CommandTimeout:
            pass
        
        out.seek(0)
        print out.read()
        #analyse(out)

def analyse(results):
    regex = re.compile(r"\[(?:[0-9A-Fa-f]{4}\.){3}[0-9A-Fa-f]([0-9A-Fa-f]{3})\] digest (\d+),(\d+),(true|false),(\d+),(\d+),(?:[0-9A-Fa-f]{4}\.){3}[0-9A-Fa-f]([0-9A-Fa-f]{3})")
    
    for line in results.readlines():
        res = regex.search(line)
        if not res: continue
        
        id = int(res.group(1), base=16)
        spot = [s for s in env.virtual_spots if s["id"] == id][0]
        
        try:
            index = spot["cycle"].index(int(res.group(2)))
            spot["hops"][index] = int(res.group(3))
            spot["coord"][index] = bool(res.group(4) == "true")
            spot["energy"][index] = float(res.group(5))
            spot["time"][index] = float(res.group(6))
            spot["parent"][index] = int(res.group(7), base=16)
        except ValueError:
            energy_value = 0
            time_value = 0
            if len(spot["cycle"]):
                energy_value = spot["energy"][-1]
                time_value = spot["time"][-1]

            spot["cycle"].append(int(res.group(2)))
            spot["hops"].append(int(res.group(3)))
            spot["coord"].append(bool(res.group(4) == "true"))
            spot["energy"].append(float(res.group(5)) - energy_value)
            spot["time"].append(float(res.group(6)) - time_value)
            spot["parent"].append(int(res.group(7), base=16))

    results.close()
    
    figure = plt.Figure()
    colors = cm.rainbow(np.linspace(0, 1, len(env.virtual_spots)))

    print len(env.virtual_spots)
    for i, color in zip(range(len(env.virtual_spots)), colors):
        spot = env.virtual_spots[i]
        ax = plt.scatter(spot["cycle"], spot["energy"],
                    s=100*np.array(spot["hops"]),
                    c=color,
                    alpha=.6)
    
    energies = reduce(lambda x, y: x+y, [s["energy"] for s in env.virtual_spots])
    plt.ylim(-100+min(energies), 100+max(energies))
    plt.show()

