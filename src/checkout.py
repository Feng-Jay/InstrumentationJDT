import subprocess
import os

resources_file = "/Users/ffengjay/Postgraduate/InstrumentationJDT/InstrumentationJDT/d4j-info/patches_inputs.csv"
lines = open(resources_file).readlines()
lines = lines[1:]

CMD = "defects4j checkout -p {proj} -v {idnum}b -w /Users/ffengjay/Postgraduate/InstrumentationJDT/tmp/defects4j_buggy/{proj}/{proj}_{idnum}_buggy/"

if not os.path.exists("/Users/ffengjay/Postgraduate/InstrumentationJDT/tmp/defects4j_buggy/"):
    os.makedirs("/Users/ffengjay/Postgraduate/InstrumentationJDT/tmp/defects4j_buggy/")

out = subprocess.run("which defects4j", shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
print(out.stdout)

for line in lines:
    bugid = line.split(",")[0]
    proj, idnum = bugid.split("_")
    if not os.path.exists("/Users/ffengjay/Postgraduate/InstrumentationJDT/tmp/defects4j_buggy/{}/".format(proj)):
        os.makedirs("/Users/ffengjay/Postgraduate/InstrumentationJDT/tmp/defects4j_buggy/{}/".format(proj))
    print("current is {}_{}".format(proj, idnum))
    print(CMD.format(proj=proj,idnum=idnum))
    out = subprocess.run(CMD.format(proj=proj, idnum=idnum), shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    print(out.stderr)
    
