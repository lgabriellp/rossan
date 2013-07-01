import tempfile
import pystache
import random
import sys

from fabric.api import *
from fabric.state import output
from fabric.contrib.console import *

#output.stdout = False

env.hosts = ["127.0.0.1"]
env.password = "surf10"
env.template_dirs = ["templates"]
env.emulation_name = "emulation"
env.manifest_name = "manifest"

env.deploy_path = "/tmp/deploy"

env.seed = -1
env.keep_addresses = False
env.run_midlets = True
env.spots_area = 100
env.spot_range = 10
env.sensors = 5
env.basestations = 1
env.max_spot_midlets = 1
env.interval = 1000

env.midlet_templates = [
    {
        "name": "HeatSensorNode",
        "path": "br.ufrj.dcc.wsn.main.HeatSensorNode",
        "type": "sensor"
    },
    {
        "name": "BaseStation",
        "path": "br.ufrj.dcc.wsn.main.BaseStation",
        "type": "basestation"
    }
]


def random_midlets(**filters):
    templates = env.midlet_templates
    for key, value in filters.items():
        templates = [t for t in templates if t[key] == value]
    
    midlets_number = random.randint(1, env.max_spot_midlets)
    midlets = random.sample(templates, midlets_number)
    for i in range(midlets_number):
        midlets[i]["index"] = i + 1

    return midlets


def random_spot(id, **filters):
    name = "{0}{1}".format("".join(filters.values()), id)
    spot = {
        "name": name,
        "version": "1.0.0",
        "vendor": "DCC-UFRJ",
        "path": "{0}".format(name),
        "build_file": "{0}/build.xml".format(name),
        "manifest_file": "resources/META-INF/manifest.mf".format(name),
        "position": random.randint(0, env.spots_area),
        "range": env.spot_range,
        "interval": env.interval,
        "midlets": random_midlets(**filters),
    }
    return spot


def configure():
    env.renderer = pystache.Renderer(search_dirs=env.template_dirs)
    seed = prompt("Whats the seed?", default=env.seed, validate=int)
    if seed < 0:
        seed = random.randint(0, sys.maxint)
    random.seed(seed)
    print "Random seed is {0}".format(seed)

    env.run_midlets = repr(env.run_midlets).lower()
    env.keep_addresses = repr(env.keep_addresses).lower()
    env.virtual_spots = []
    for i in range(sensors):
        env.virtual_spots.append(random_spot(i, type="sensor"))

    for i in range(basestations):
        env.virtual_spots.append(random_spot(i, type="basestation"))


def render(name, context):
    rendered = tempfile.TemporaryFile("w+")
    rendered.write(env.renderer.render_name(name, context))
    rendered.seek(0)
    return rendered


@task
def make():
    clean()
    configure()

    emulation = render("%(emulation_name)s" % env, env)

    run("mkdir %(deploy_path)s -p" % env)
    with cd("%(deploy_path)s" % env):
        put(emulation, "%(emulation_name)s.xml" % env)
        emulation.close()

        local("git archive HEAD --prefix=repo/ -o repo.tgz")
        put("repo.tgz", "repo.tgz")
        run("tar xzvf repo.tgz")
        run("cp repo/build.xml .")

        for spot in env.virtual_spots:
            run("cp repo %(path)s -r" % spot)
            with cd("%(path)s" % spot):
                manifest = render("%(manifest_name)s" % env, spot)
                put(manifest, "%(manifest_file)s" % spot)
                manifest.close()

        run("rm repo.tgz -rf")
        local("rm repo.tgz")

@task
def clean():
    run("rm %(deploy_path)s -rf" % env)

@task
def start():
    with cd("%(deploy_path)s" % env):
        run("DISPLAY=:0.0 ant solarium -Dconfig.file=%(emulation_name)s.xml" % env)


