import sys

header = """<?xml version="1.0" encoding="UTF-8"?>
<virtual-config keep-addresses="false" run-midlets="true">"""

footer = """
</virtual-config>"""

spot_config = """
    <virtual-spot>
        <build file="build.xml"/>
        <midlet name="br.ufrj.dcc.wsn.main.{0}"/>
    </virtual-spot>"""

ap_spot_name = "AccessPointNode"
sensor_spot_name = "HeatSensorNode"
spot_number = input("Numero de spots:")

config = open("virtual-config-{0}.xml".format(spot_number), "w")
config.write(header)

for i in xrange(1, spot_number):
    config.write(spot_config.format(sensor_spot_name))

config.write(spot_config.format(ap_spot_name))
config.write(footer)
config.close()
