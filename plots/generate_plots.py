from scipy import stats
import numpy as np
from scipy.stats import kstest
from matplotlib import pyplot as plt
import math
import os

lines = open("seminar_tests_outputs.txt", "r").readlines()

contention_managers = ["Kindergarten", "Timestamp", "Less", "Polka", "Karma", "Polite", "Passive"]
use_cases = []
tests_cms = []
cm_clients = []
executions_time = []
aborts = []

for line in lines:
    if line.startswith("NOBJSERVER:"):
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
objs_per_transaction = [5, 10]
objs_per_server = [100, 500]
number_of_clients = [2, 4, 8]
test_cases_dict = {}

for wp in writes_percentage:
    for opt in objs_per_transaction:
        for ops in objs_per_server:
            test_cases_dict[f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}"] = {
                cm: {noc: [] for noc in number_of_clients} for cm in contention_managers
            }
print(test_cases_dict)

i = 0

for wp in writes_percentage:
    for opt in objs_per_transaction:
        for ops in objs_per_server:
            for cm in contention_managers:
                for noc in number_of_clients:
                    for test in range(5):
                        #test_cases_dict[f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}"][cm][noc].append(executions_time[i])
                        #print(executions_time[i])
                        test_cases_dict[f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}"][cm][noc].append(
                            int(executions_time[i].split("Time of execution: ")[1].split(" milliseconds")[0]))
                        i+=1

print(test_cases_dict)

test_cases_avgs_dict = {}

for wp in writes_percentage:
    for opt in objs_per_transaction:
        for ops in objs_per_server:
            test_cases_avgs_dict[f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}"] = {
                cm: {noc: 0 for noc in number_of_clients} for cm in contention_managers
            }

for wp in writes_percentage:
    for opt in objs_per_transaction:
        for ops in objs_per_server:
            for cm in contention_managers:
                for noc in number_of_clients:
                    avg = 0
                    for test in range(5):
                        avg += test_cases_dict[f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}"][cm][noc][test]
                    avg /= 5*1000
                    test_cases_avgs_dict[f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}"][cm][noc] = round(avg, 2)

print(test_cases_avgs_dict)

counts = [[] for i in range(8)]

print(counts)

i = 0
for wp in writes_percentage:
    for opt in objs_per_transaction:
        for ops in objs_per_server:
            for cm in contention_managers:
                j = 0
                count = []
                for noc in number_of_clients:
                    count.append(test_cases_avgs_dict[f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}"][cm][noc])
                    j += 1
                print(count)
                counts[i].append(count)
            i+=1

print(counts)

i = 0
n = 0
for wp in writes_percentage:
    for opt in objs_per_transaction:
        for ops in objs_per_server:
            avgs = {}
            j = 0
            for cm in contention_managers:
                avgs[cm] = counts[i][j]
                j+=1

            x = np.arange(len(number_of_clients))  # the label locations
            width = 0.1  # the width of the bars
            multiplier = 0

            fig, ax = plt.subplots(layout='constrained')

            for attribute, measurement in avgs.items():
                offset = width * multiplier
                rects = ax.bar(x + offset, measurement, width, label=attribute)
                ax.bar_label(rects, padding=3)
                multiplier += 1

            # Add some text for labels, title and custom x-axis tick labels, etc.
            ax.set_ylabel('Time (seconds)')
            ax.set_title(f"Objects per server: {ops}, Percentage of writes: {wp} %, Objects per transaction:{opt}")
            ax.set_xticks(x + width, number_of_clients)
            ax.legend(loc='upper right', ncols=3)
            ax.set_ylim(0, 90)

            fig.set_figheight(10)
            fig.set_figwidth(19)
            fig.savefig(f"NOBJSERVER: {ops}, WRITES: {wp}, NOBJTRANS:{opt}.png")
            n+=1
            i+=1

print(n)
plt.show()


