from singleserver import SingleServer
def main():
    #opening input and output files
    infile=open('mm1.in', 'r')
    outfile=open('mm2.out', 'w')

    #Read input parameters from infile
    param_list=infile.readlines()
    #stripping the '\n' characters from the list items
    param_list=[(i.rstrip('\n')) for i in param_list]

    #creating object file of Single server
    #attributes are initialized
    server = SingleServer()

    #initializing the event lists
    server.initialize(float(param_list[0]), float(param_list[1]), int(param_list[2]))

    #Write headings and input parameters
    outfile.writelines('Single server queuing system\n\n\n')
    outfile.writelines('Mean inter-arrival time {:11.3f}\n'.format(float(param_list[0])))
    outfile.writelines('Mean service time {:16.3f}\n'.format(float(param_list[1])))
    outfile.writelines('Number of customers {:14d}\n'.format(int(param_list[2])))


    #Run the simulation while more delays are still needed
    while (server.get_num_custs_delayed() < server.get_num_delays_required()):
        #determine the next event
        server.timing(outfile)

        #update time average statistical accumulators
        server.update_time_avg_stats()

        #invoke appropriate event function
        if(server.get_next_event_type()==1):
            server.arrive(outfile)
        elif(server.get_next_event_type() == 2):
            server.depart()

    #invoke report generator and end the Simulation
    server.report(outfile)
    infile.close()
    outfile.close()

main()
