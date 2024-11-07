from scipy import stats
import numpy as np
from scipy.stats import kstest
from matplotlib import pyplot as plt
import math
import os
import sys

print(sys.argv)
cm_selected = sys.argv[1]
dynamic_cm_params_test_filename = "tests_results/generic_" + cm_selected + "_out_10tests.txt"

lines = open(dynamic_cm_params_test_filename, "r").readlines()

contention_managers = ["Kindergarten", "Timestamp", "Polka", "Karma", "Polite", "Passive"]
use_cases = []
tests_cms = []
cm_clients = []
executions_time = []
aborts = []
cm_params_configs = []

for line in lines:
    if line.startswith("NOBJSERVER:"):
        use_cases.append(line)
    elif line.startswith("Total of aborts"):
        aborts.append(line)
    elif line.startswith("Test"):
        tests_cms.append(line)
        cm_param_config = line.split(":")[1].strip()
        if cm_param_config not in cm_params_configs:
            cm_params_configs.append(cm_param_config)
    else:
        if line.startswith("TRMI"):
            cm_clients.append(line)
        if "Time of execution" in line:
            executions_time.append(line)

print(len(use_cases))
print(len(tests_cms))
print(len(cm_clients))
print(len(executions_time))
print(len(aborts))

for use_case in use_cases:
    print(use_case)
    for i in range(len(cm_clients)):
        print(i)
        print(tests_cms[i], end="")
        print(cm_clients[i], end="")
        print(executions_time[i], end="")
        print(aborts[i], end="")

writes_percentage = [20, 50]
objs_per_transaction = [5, 20] #[5, 10]
objs_per_server = [100, 500]
number_of_clients = [2, 4, 8, 16]
test_cases_dict = {}

print(cm_params_configs)

contention_manager = cm_clients[0].split(" ")[0].split("TRMI")[1].split("\t")[0]

param0_title = ""
param1_title = ""

maxaborts_mindelay_delay = 0
maxdelay_intervals = 0

maxaborts_mindelay_delay_arr = []
maxdelay_intervals_arr = []

if "; " in cm_params_configs[0]:
    two_params = cm_params_configs[0].split("; ")
    param0 = two_params[0]
    param1 = two_params[1]
    param0_title = param0.split(" ")[1]
    param1_title = param1.split(" ")[1]
else:
    param0 = cm_params_configs[0].split(" ")
    param0_title = param0[0]

if "; " in cm_params_configs[0]:
    for cm_param_config in cm_params_configs:
        two_params = cm_param_config.split("; ")
        param0 = two_params[0]
        param1 = two_params[1]

        maxaborts_mindelay_delay = int(param0.split(" ")[0])
        if maxaborts_mindelay_delay not in maxaborts_mindelay_delay_arr:
            maxaborts_mindelay_delay_arr.append(maxaborts_mindelay_delay)
        maxdelay_intervals = int(param1.split(" ")[0])
        if maxdelay_intervals not in maxdelay_intervals_arr:
            maxdelay_intervals_arr.append(maxdelay_intervals)
else:
    for cm_param_config in cm_params_configs:
        param0 = cm_params_configs[0].split(" ")

        maxaborts_mindelay_delay = int(param0[0])
        if maxaborts_mindelay_delay not in maxaborts_mindelay_delay_arr:
            maxaborts_mindelay_delay_arr.append(maxaborts_mindelay_delay)


print(param0_title)
print(maxaborts_mindelay_delay_arr)
print(param1_title)
print(maxdelay_intervals_arr)


for wp in writes_percentage:
    for opt in objs_per_transaction:
        for ops in objs_per_server:
            test_cases_dict[f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}"] = {
                 cpc: {noc: [] for noc in number_of_clients} for cpc in cm_params_configs
            }
print(test_cases_dict)

i = 0

for wp in writes_percentage:
    for opt in objs_per_transaction:
        for ops in objs_per_server:
            for cpc in cm_params_configs:
                for noc in number_of_clients:
                    for test in range(10):
                        print(executions_time[i])
                        test_cases_dict[f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}"][cpc][noc].append(
                            int(executions_time[i].split("Time of execution: ")[1].split(" milliseconds")[0]))
                        i+=1
                        print(i)

print(test_cases_dict)

test_cases_avgs_dict = {}

for wp in writes_percentage:
    for opt in objs_per_transaction:
        for ops in objs_per_server:
            test_cases_avgs_dict[f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}"] = {
                    cpc: {noc: [] for noc in number_of_clients} for cpc in cm_params_configs
            }

for wp in writes_percentage:
    for opt in objs_per_transaction:
        for ops in objs_per_server:
            for cpc in cm_params_configs:
                for noc in number_of_clients:
                    avg = 0
                    for test in range(10):
                        avg += test_cases_dict[f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}"][cpc][noc][test]
                    avg /= 5*1000
                    test_cases_avgs_dict[f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}"][cpc][noc] = round(avg, 2)

print(test_cases_avgs_dict)

counts = [[] for i in range(8)]

print(counts)

i = 0
for wp in writes_percentage:
    for opt in objs_per_transaction:
        for ops in objs_per_server:
            for cpc in cm_params_configs:
                j = 0
                count = []
                for noc in number_of_clients:
                    count.append(test_cases_avgs_dict[f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}"][cpc][noc])
                    j += 1
                print(count)
                counts[i].append(count)
            i+=1

print(counts)

if not os.path.exists("10Tests"):
    os.makedirs("10Tests")
if not os.path.exists("10Tests/" + contention_manager):
    os.makedirs("10Tests/" + contention_manager)

i = 0
n = 0
for wp in writes_percentage:
    for opt in objs_per_transaction:
        for ops in objs_per_server:
            avgs = {}
            j = 0
            max = 0
            for cpc in cm_params_configs:
                print(counts[i][j])
                local_max = np.array(counts[i][j]).max()
                max = local_max if local_max > max else max  
                avgs[cpc] = counts[i][j]
                j+=1

            x = np.arange(len(number_of_clients))  # the label locations
            width = 0.1  # the width of the bars
            multiplier = 0

            fig, ax = plt.subplots(layout='constrained')

            for attribute, measurement in avgs.items():
                offset = width * multiplier
                rects = ax.bar(x + offset, measurement, width, label=attribute)
                ax.bar_label(rects, padding=0)
                multiplier += 1

            # Add some text for labels, title and custom x-axis tick labels, etc.
            ax.set_ylabel('Time (seconds)', fontsize=20)
            ax.set_title(f"{contention_manager}\nObjects per server: {ops}, Percentage of writes: {wp} %, Objects per transaction:{opt}", fontsize=20)
            ax.set_xticks(x + width, number_of_clients)
            ax.legend(loc='upper right', ncols=1, prop={'size': 18})
            ax.set_ylim(0, max + 10)
            ax.tick_params(axis='x', labelsize=20)
            ax.tick_params(axis='y', labelsize=20)

            fig.set_figheight(10)
            fig.set_figwidth(19)

            fig.savefig(f"10Tests/{contention_manager}/NOBJSERVER_{ops},WRITES_{wp},NOBJTRANS_{opt}.png")
            n+=1
            i+=1

print(n)
plt.show()


