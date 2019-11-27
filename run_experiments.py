import sys
import os
import time
import math
from multiprocessing import Pool

def getFreeMemory(machine):
	out = os.popen("ssh "+machine+" \"awk '/MemAvailable/ {print \$2/1024}' /proc/meminfo\"").read()
	return machine, float(out.strip())-4072

if __name__=="__main__":
	desired_num_hubs = [10000, 5000, 2500, 1000, 100,]
	min_max_connections = [(2,4), (5,10), (10,15), (20,25)]
	cache_sizes = [40, 100, 200, 400]
	
	for num_hubs, connections, cache_size in zip(desired_num_hubs, min_max_connections, cache_sizes):
		p = Pool(225)
		with open(sys.argv[1], "r") as f:
			machines = [m.strip() for m in f.readlines()]
		
		out = p.map(getFreeMemory, machines)
		p.close()
		p.join()
		
		total_mem = sum([x[1] for x in out])
		current_num_hubs = 0
		assignments = []
		for machine, mem in out:
			num_workers = int(min(max(100,math.ceil((mem/total_mem)*num_hubs)),mem/20))
			assignments.append((machine,min(num_workers,num_hubs-current_num_hubs)))
			current_num_hubs += min(num_workers,num_hubs-current_num_hubs)
			print(assignments)
			if current_num_hubs >= num_hubs:
				break

		filename = "./results/results_"+str(num_hubs)+"_"+str(connections[1])+".csv"
		os.system("xterm -e 'ssh topeka \"cd CS555-final; sh automatedRegistry.sh "+str(connections[0])+" "+str(connections[1])+" "+filename+" \"' &")
		time.sleep(60)
		for assignment in assignments:
			os.system("ssh "+assignment[0]+" 'cd CS555-final; sh lotsofpeers.sh "+str(assignment[1])+" "+str(cache_size)+"' &")
		time.sleep(900)
		os.system("sh angryshutdown.sh lattice_workers.txt")
		os.system("sh angryshutdown.sh workers.txt")
