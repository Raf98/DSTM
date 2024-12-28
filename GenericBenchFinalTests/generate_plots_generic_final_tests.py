from scipy import stats
import numpy as np
from scipy.stats import kstest
from matplotlib import pyplot as plt
import math
import os
import sys

print(sys.argv)
cm_selected = sys.argv[1]
generic_final_test_filename = "tests_results/generic_" + cm_selected + "_final_tests.txt"
#generic_final_test_filename = "kindergarten_out_test_list.txt"
#generic_final_test_filename = "kindergarten_out_test_set.txt"

lines = open(generic_final_test_filename, "r").readlines()

contention_managers = ["Agressive", "Less", "Kindergarten", "Timestamp", "Polka", "Karma", "Polite", "Passive"]
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

# for use_case in use_cases:
#     print(use_case)
#     for i in range(len(cm_clients)):
#         print(i)
#         print(tests_cms[i], end="")
#         print(cm_clients[i], end="")
#         print(executions_time[i], end="")
#         print(aborts[i], end="")

writes_percentage = [20, 50]
objs_per_transaction = [5, 20]
objs_per_server = [100, 500]
number_of_clients = [2, 4, 8, 16]
number_of_tests = 10
test_cases_dict = {}

print(cm_params_configs)

contention_manager = cm_clients[0].split(" ")[0].split("TRMI")[1].split("\t")[0]

param0_title = ""
param1_title = ""

maxaborts_mindelay_delay = 0
maxdelay_intervals = 0

maxaborts_mindelay_delay = 0
maxdelay_intervals = 0

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
    two_params = cm_params_configs[0].split("; ")
    param0 = two_params[0]
    param1 = two_params[1]

    maxaborts_mindelay_delay = int(param0.split(" ")[0])
    maxdelay_intervals = int(param1.split(" ")[0])
else:
    for cm_param_config in cm_params_configs:
        param0 = cm_params_configs[0].split(" ")

        maxaborts_mindelay_delay = int(param0[0])


# print(param0_title)
# print(maxaborts_mindelay_delay_arr)
# print(param1_title)
# print(maxdelay_intervals_arr)


for wp in writes_percentage:
    for opt in objs_per_transaction:
        for ops in objs_per_server:
            test_cases_dict[f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}"] = {
                 noc: [] for noc in number_of_clients
            }
#print(test_cases_dict)

i = 0

for wp in writes_percentage:
    for opt in objs_per_transaction:
        for ops in objs_per_server:
            for noc in number_of_clients:
                for test in range(10):
                    print(executions_time[i])
                    test_cases_dict[f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}"][noc].append(
                        int(executions_time[i].split("Time of execution: ")[1].split(" milliseconds")[0]))
                    i+=1
                    print(i)

#print(test_cases_dict)

test_cases_avgs_dict = {}

for wp in writes_percentage:
    for opt in objs_per_transaction:
        for ops in objs_per_server:
            test_cases_avgs_dict[f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}"] = {
                noc: {"avg": 0, "max_error": 0} for noc in number_of_clients
            }

for wp in writes_percentage:
    for opt in objs_per_transaction:
        for ops in objs_per_server:
            for noc in number_of_clients:
                avg = 0
                min_value = 999999
                max_value = -1
                for test in range(number_of_tests):
                    avg += test_cases_dict[f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}"][noc][test]
                    max_value = max(max_value, test_cases_dict[f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}"][noc][test])
                    min_value = min(min_value, test_cases_dict[f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}"][noc][test])
                avg /= number_of_tests * 1000
                #print("MIN VALUE: " + str(min_value))
                min_value /= 1000
                max_value /= 1000
                max_error = (max_value - min_value)
                test_cases_avgs_dict[f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}"][noc]["avg"] = round(avg, 2)
                test_cases_avgs_dict[f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}"][noc]["min"] = round(min_value, 2)
                test_cases_avgs_dict[f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}"][noc]["max"] = round(max_value, 2)
                test_cases_avgs_dict[f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}"][noc]["lower_bound"] = round(avg - min_value, 2)
                test_cases_avgs_dict[f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}"][noc]["upper_bound"] = round(max_value - avg, 2)
                test_cases_avgs_dict[f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}"][noc]["max_error"] = round(max_error, 2)
#print("AVERAGES:")
#print(test_cases_avgs_dict)

counts = []
mins = []
maxs = []
lowers = []
uppers = []
errors = []

#print(counts)

i = 0
for wp in writes_percentage:
    for opt in objs_per_transaction:
        for ops in objs_per_server:
            j = 0
            count = []
            min_value = []
            max_value = []
            lower_bound = []
            upper_bound = []
            error = []
            for noc in number_of_clients:
                count.append(test_cases_avgs_dict[f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}"][noc]["avg"])
                min_value.append(test_cases_avgs_dict[f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}"][noc]["min"])
                max_value.append(test_cases_avgs_dict[f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}"][noc]["max"])
                lower_bound.append(test_cases_avgs_dict[f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}"][noc]["lower_bound"])
                upper_bound.append(test_cases_avgs_dict[f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}"][noc]["upper_bound"])
                error.append(test_cases_avgs_dict[f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}"][noc]["max_error"])
                j += 1
            #print(count)
            counts.append(count)
            mins.append(min_value)
            maxs.append(max_value)
            lowers.append(lower_bound)
            uppers.append(upper_bound)
            errors.append(error)

#print(counts)

if not os.path.exists(contention_manager):
    os.makedirs(contention_manager)

multiply_factor = 1.15 if maxdelay_intervals == 0 else 1.08 


i = 0
n = 0
for wp in writes_percentage:
    for opt in objs_per_transaction:
        for ops in objs_per_server:
            avgs = 0
            min_values = 0
            max_values = 0
            lower_bounds = 0
            upper_bounds = 0
            max_errors = 0
            max = 0

            print(f"Objects per server: {ops}, Percentage of writes: {wp} %, Objects per transaction:{opt}")
            #print(counts[i])
            local_max = np.array(maxs[i]).max() #np.array(counts[i][j]).max() + np.array(errors[i][j]).max()
            max = local_max if local_max > max else max
            max *= multiply_factor
            avgs = counts[i]
            min_values = mins[i]
            max_values = maxs[i]
            lower_bounds = lowers[i]
            upper_bounds = uppers[i]
            max_errors = errors[i]

            x = np.arange(len(number_of_clients))  # the label locations
            width = 0.1  # the width of the bars
            multiplier = 0

            fig, ax = plt.subplots(layout='constrained')

            #print(avgs)
            offset = width * multiplier
            #y_lower_bound = np.array(avgs[attribute]) - np.array(min_values[attribute])
            #y_upper_bound =np.array(max_values[attribute]) - np.array(avgs[attribute])
            error_bounds = [lower_bounds, upper_bounds]
            #print(error_bounds)

            bars = ax.bar(x = x + offset, height = avgs, width = width, yerr = error_bounds, 
                           capsize = 10, label = number_of_clients, edgecolor = 'black', zorder=3)
            for index, bar in enumerate(bars):
                height = bar.get_height()
                print(max_error)
                max_error = max_errors[index]
                lower_bound = lower_bounds[index]
                upper_bound = upper_bounds[index]
                ax.text(x = bar.get_x() + bar.get_width()/2, y = height, s = f'{height:.2f}', 
                        horizontalalignment='center', verticalalignment='bottom')
                
                if lower_bound > max / 10:
                    min_value = min_values[index]
                    #print("MIN VALUE: " + str(min_value))
                    ax.text(x = bar.get_x() + bar.get_width()/2, y = min_value, s = f'{min_value:.2f}', 
                        horizontalalignment='center', verticalalignment='bottom')
                
                if upper_bound > max / 10:
                    max_value = max_values[index]
                    ax.text(x = bar.get_x() + bar.get_width()/2, y = max_value, s = f'{max_value:.2f}', 
                        horizontalalignment='center', verticalalignment='bottom')
            #ax.bar_label(bars, padding=0)
                multiplier += 1

            # Add some text for labels, title and custom x-axis tick labels, etc.
            ax.set_ylabel('Time (seconds)', fontsize=20)
            ax.set_title(f"{contention_manager}\nObjects per server: {ops}, Percentage of writes: {wp} %, Objects per transaction:{opt}", fontsize=20)
            ax.set_xticks(x + width, number_of_clients)
            ax.legend(loc='upper right', ncols=1, prop={'size': 18})
            ax.set_ylim(0, max)
            ax.tick_params(axis='x', labelsize=20)
            ax.tick_params(axis='y', labelsize=20)
            ax.grid(zorder=0)

            fig.set_figheight(10)
            fig.set_figwidth(19)

            fig.savefig(f"{contention_manager}/Set/NOBJSERVER_{ops},WRITES_{wp},NOBJTRANS_{opt}.png")
            n+=1
            i+=1

print(n)
plt.show()