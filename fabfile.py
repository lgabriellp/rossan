import tempfile
import pystache

from fabric.api import *
from fabric.contrib.console import *

env.hosts = ["127.0.0.1"]
env.template_dirs = ["templates"]
env.config_name = "emulation"
env.deploy_path = "/tmp/deploy"
env.midlets = {
    "HeatSensorNode": "br.ufrj.dcc.wsn.main.HeatSensorNode",
    "BaseStation": "br.ufrj.dcc.wsn.main.BaseStation"
}

def configure():
    env.renderer = pystache.Renderer(search_dirs=env.template_dirs)
    
    env.commit = confirm("Commit first?", default=False)
    env.keep_addresses = confirm("Keep spot adresses?", default=False)
    env.run_midlets = confirm("Run midlets?")

    env.virtual_spots = []
    for name, path in env.midlets.items():
        number = prompt("How many %s midlets?" % name, default=1,
                        validate=int)

        for i in range(number):
            env.virtual_spots.append({
                "midlet-name": path,
                "build-file": "%s/build.xml" % name.lower()
            })


@task
def build_emulation():
    configure()

    config = tempfile.TemporaryFile("w+")
    config.write(env.renderer.render_name("%(config_name)s" % env, env))
    config.seek(0)

    run("mkdir %(deploy_path)s -p" % env)
    with cd("%(deploy_path)s" % env):
        put(config, "%(config_name)s.xml" % env)
        
@task
def start():
    run("ant solarium -Dconfig.file={0}.xml".format(CONFIG_FILE_NAME))
