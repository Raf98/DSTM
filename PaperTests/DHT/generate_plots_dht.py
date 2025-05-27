from scipy import stats
import numpy as np
from scipy.stats import kstest
from matplotlib import pyplot as plt
import math
import os

# "Less", "Karma", 
contention_managers = ["Aggressive", "Kindergarten", "Passive", "Polite", "Polka", "Timestamp"]

use_cases = []
tests_cms = []
cm_clients = []
executions_time = []
aborts = []


for cm in contention_managers:
    dht_tests_filename = "tests_results/dht_" + cm.lower() + ".txt"

    lines = open(dht_tests_filename, "r").readlines()

    for line in lines:
        if line.startswith("NKEYS:"):
            use_cases.append(line)
        elif line.startswith("Total of aborts"):
            aborts.append(line)
        elif line.startswith("Test"):
            tests_cms.append(line)
        else:
            if line.startswith("TRMI"):
                cm_clients.append(line)
            if "Time of execution" in line:
                executions_time.append(line)

    print(len(tests_cms))

print(len(use_cases))
print(len(tests_cms))
print(len(cm_clients))
print(len(executions_time))
print(len(aborts))

'''
for use_case in use_cases:
    print(use_case)
    for i in range(len(cm_clients)):
        print(i)
        print(tests_cms[i], end="")
        print(cm_clients[i], end="")
        print(executions_time[i], end="")
        print(aborts[i], end="")
'''

writes_percentage = [20, 50]
objs_per_transaction = [20, 50]
number_of_keys = 20000
number_of_ht_entries = [128, 1567]
number_of_clients = [2, 4, 8, 16]
number_of_tests = 10
test_cases_dict = {}

for wp in writes_percentage:
    for opt in objs_per_transaction:
        for nohte in number_of_ht_entries:
            test_cases_dict[f"NKEYS: {number_of_keys}, NHTENTRIES: {nohte}, WRITES: {wp}, NOBJTRANS:{opt}"] = {
                cm: {noc: [] for noc in number_of_clients} for cm in contention_managers
            }
print(test_cases_dict)

i = 0

for cm in contention_managers:
    for wp in writes_percentage:
        for opt in objs_per_transaction:
            for nohte in number_of_ht_entries:
                for noc in number_of_clients:
                    for test in range(number_of_tests):
                        print(cm)
                        print(tests_cms[i])
                        print(executions_time[i])
                        test_cases_dict[f"NKEYS: {number_of_keys}, NHTENTRIES: {nohte}, WRITES: {wp}, NOBJTRANS:{opt}"][cm][noc].append(
                            int(executions_time[i].split("Time of execution: ")[1].split(" milliseconds")[0]))
                        i+=1

print(test_cases_dict)

test_cases_avgs_dict = {}

for wp in writes_percentage:
    for opt in objs_per_transaction:
        for nohte in number_of_ht_entries:
            test_cases_avgs_dict[f"NKEYS: {number_of_keys}, NHTENTRIES: {nohte}, WRITES: {wp}, NOBJTRANS:{opt}"] = {
                cm: {noc: {"avg": 0} for noc in number_of_clients} for cm in contention_managers
            }

same_contention_makespan = {number_of_ht_entries[0]: 0, number_of_ht_entries[1]: 0}
same_contention_same_client_makespan = {number_of_ht_entries[0]: {noc: 0 for noc in number_of_clients}, 
                                        number_of_ht_entries[1]: {noc: 0 for noc in number_of_clients}}

for wp in writes_percentage:
    for opt in objs_per_transaction:
        for nohte in number_of_ht_entries:
            for cm in contention_managers:
                for noc in number_of_clients:
                    avg = 0
                    min_value = 999999
                    max_value = -1
                    for test in range(number_of_tests):
                        avg += test_cases_dict[f"NKEYS: {number_of_keys}, NHTENTRIES: {nohte}, WRITES: {wp}, NOBJTRANS:{opt}"][cm][noc][test]
                        max_value = max(max_value, test_cases_dict[f"NKEYS: {number_of_keys}, NHTENTRIES: {nohte}, WRITES: {wp}, NOBJTRANS:{opt}"][cm][noc][test])
                        min_value = min(min_value, test_cases_dict[f"NKEYS: {number_of_keys}, NHTENTRIES: {nohte}, WRITES: {wp}, NOBJTRANS:{opt}"][cm][noc][test])
                    avg /= number_of_tests*1000
                    min_value /= 1000
                    max_value /= 1000

                    same_contention_makespan[nohte] += avg
                    same_contention_same_client_makespan[nohte][noc] += avg

                    test_cases_avgs_dict[f"NKEYS: {number_of_keys}, NHTENTRIES: {nohte}, WRITES: {wp}, NOBJTRANS:{opt}"][cm][noc]["avg"] = round(avg, 2)
                    test_cases_avgs_dict[f"NKEYS: {number_of_keys}, NHTENTRIES: {nohte}, WRITES: {wp}, NOBJTRANS:{opt}"][cm][noc]["min"] = round(min_value, 2)
                    test_cases_avgs_dict[f"NKEYS: {number_of_keys}, NHTENTRIES: {nohte}, WRITES: {wp}, NOBJTRANS:{opt}"][cm][noc]["max"] = round(max_value, 2)
                    test_cases_avgs_dict[f"NKEYS: {number_of_keys}, NHTENTRIES: {nohte}, WRITES: {wp}, NOBJTRANS:{opt}"][cm][noc]["lower_bound"] = round(avg - min_value, 2)
                    test_cases_avgs_dict[f"NKEYS: {number_of_keys}, NHTENTRIES: {nohte}, WRITES: {wp}, NOBJTRANS:{opt}"][cm][noc]["upper_bound"] = round(max_value - avg, 2)

print(test_cases_avgs_dict)

counts = [[] for i in range(8)]
mins = [[] for i in range(8)]
maxs = [[] for i in range(8)]
lowers = [[] for i in range(8)]
uppers = [[] for i in range(8)]

print(counts)

i = 0
for wp in writes_percentage:
    for opt in objs_per_transaction:
        for nohte in number_of_ht_entries:
            for cm in contention_managers:
                j = 0
                count = []
                count = []
                min_value = []
                max_value = []
                lower_bound = []
                upper_bound = []
                for noc in number_of_clients:
                    count.append(test_cases_avgs_dict[f"NKEYS: {number_of_keys}, NHTENTRIES: {nohte}, WRITES: {wp}, NOBJTRANS:{opt}"][cm][noc]["avg"])
                    min_value.append(test_cases_avgs_dict[f"NKEYS: {number_of_keys}, NHTENTRIES: {nohte}, WRITES: {wp}, NOBJTRANS:{opt}"][cm][noc]["min"])
                    max_value.append(test_cases_avgs_dict[f"NKEYS: {number_of_keys}, NHTENTRIES: {nohte}, WRITES: {wp}, NOBJTRANS:{opt}"][cm][noc]["max"])
                    lower_bound.append(test_cases_avgs_dict[f"NKEYS: {number_of_keys}, NHTENTRIES: {nohte}, WRITES: {wp}, NOBJTRANS:{opt}"][cm][noc]["lower_bound"])
                    upper_bound.append(test_cases_avgs_dict[f"NKEYS: {number_of_keys}, NHTENTRIES: {nohte}, WRITES: {wp}, NOBJTRANS:{opt}"][cm][noc]["upper_bound"])
                    j += 1
                #print(count)
                counts[i].append(count)
                mins[i].append(min_value)
                maxs[i].append(max_value)
                lowers[i].append(lower_bound)
                uppers[i].append(upper_bound)
            i+=1

print(counts)

if not os.path.exists("Plots"):
    os.makedirs("Plots")

multiply_factor = 1.07

i = 0
n = 0
for wp in writes_percentage:
    for opt in objs_per_transaction:
        for nohte in number_of_ht_entries:
            avgs = {}
            min_values = {}
            max_values = {}
            lower_bounds = {}
            upper_bounds = {}
            j = 0
            max = 0

            print(f"NKEYS: {number_of_keys}, NHTENTRIES: {nohte}, Percentage of writes: {wp} %, Objects per transaction:{opt}")
            for cm in contention_managers:
                print(counts[i][j])
                local_max = np.array(maxs[i][j]).max()
                max = local_max if local_max > max else max
                max *= multiply_factor

                avgs[cm] = counts[i][j]
                min_values[cm] = mins[i][j]
                max_values[cm] = maxs[i][j]
                lower_bounds[cm] = lowers[i][j]
                upper_bounds[cm] = uppers[i][j]

                j+=1

            x = np.arange(len(number_of_clients))  # the label locations
            width = 0.1  # the width of the bars
            multiplier = 0

            fig, ax = plt.subplots(layout='constrained')

            for attribute, measurement in avgs.items():
                print(attribute)
                print(measurement)
                offset = width * multiplier
                error_bounds = [lower_bounds[attribute], upper_bounds[attribute]]

                bars = ax.bar(x = x + offset, height = measurement, width = width, yerr = error_bounds, 
                               capsize = 10, label = attribute, edgecolor = 'black', zorder=3)
                for index, bar in enumerate(bars):
                    height = bar.get_height()
                    lower_bound = lower_bounds[attribute][index]
                    upper_bound = upper_bounds[attribute][index]

                    ax.text(x = bar.get_x() + bar.get_width()/2, y = height, s = f'{height:.2f}', 
                            horizontalalignment='center', verticalalignment='bottom')
                    
                    if lower_bound > max / 10:
                        min_value = min_values[attribute][index]
                        #print("MIN VALUE: " + str(min_value))
                        ax.text(x = bar.get_x() + bar.get_width()/2, y = min_value, s = f'{min_value:.2f}', 
                            horizontalalignment='center', verticalalignment='bottom')
                    
                    if upper_bound > max / 10:
                        max_value = max_values[attribute][index]
                        ax.text(x = bar.get_x() + bar.get_width()/2, y = max_value, s = f'{max_value:.2f}', 
                            horizontalalignment='center', verticalalignment='bottom')
                #ax.bar_label(bars, padding=0)
                multiplier += 1

            # Add some text for labels, title and custom x-axis tick labels, etc.
            ax.set_ylabel('Time (seconds)', fontsize=24)
            ax.set_xlabel('Number of clients', fontsize=24)
            ax.set_title(f"DHT\nNumber of entries per HT: {nohte}, Percentage of writes: {wp} %, Objects per transaction:{opt}", fontsize=24)
            ax.set_xticks(x + width, number_of_clients)
            ax.legend(loc='upper right', ncols=3, prop={'size': 24})
            ax.set_ylim(0, max)
            ax.tick_params(axis='x', labelsize=24)
            ax.tick_params(axis='y', labelsize=24)
            ax.grid(zorder=0)

            fig.set_figheight(10)
            fig.set_figwidth(19)
            fig.savefig(f"Plots/NKEYS: {number_of_keys}, NHTENTRIES: {nohte}, WRITES: {wp}, NOBJTRANS:{opt}.png")
            n+=1
            i+=1

print(n)

print("HIGH CONTENTION MAKESPAN AVERAGE: " + str(same_contention_makespan[number_of_ht_entries[0]] / (4 * 4 * 8)))
print("LOW CONTENTION MAKESPAN AVERAGE: " + str(same_contention_makespan[number_of_ht_entries[1]] / (4 * 4 * 8)))

for noc in number_of_clients:
    print(str(noc) + " CLIENTS; HIGH CONTENTION MAKESPAN AVERAGE: " + str(same_contention_same_client_makespan[number_of_ht_entries[0]][noc] / (4 * 8)))
    print(str(noc) + " CLIENTS; LOW CONTENTION MAKESPAN AVERAGE: " + str(same_contention_same_client_makespan[number_of_ht_entries[1]][noc] / (4 * 8)))

plt.show()