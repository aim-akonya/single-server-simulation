import numpy as np

class SingleServer(object):
    #intialization function
    def __init__ (self):
        self.Q_LIMIT = 1000 #Limit on queue length
        #mnemomics for server busy and  idle
        self.BUSY=1
        self.IDLE=0

        #initialize the simulation clock
        self.time = 0.0
        #initialize the state variables
        self.server_status = self.IDLE
        self.num_in_q = 0
        self.time_last_event= 0.0

        #intialize the statistical counters
        self.num_custs_delayed = 0
        self.total_of_delays = 0.0
        self.area_num_in_q = 0.0
        self.area_server_status = 0.0

        self.mean_interarrival = 0.0
        self.num_delays_required = 0
        self.next_event_type=0.0
        self.num_events = 2
        self.mean_service = None

        #initialize the event lists
        self.time_arrival = list(np.zeros(self.Q_LIMIT + 1))
        self.time_next_event = list(np.zeros(3))




    def initialize(self, value1, value2, value3):
        #initializing the event lists
        self.time_next_event[1] = self.time + self.expon(self.mean_interarrival)
        self.time_next_event[2] = 1.0e+30
        #assigning values to mean_interarrival, mean_service and num_delays_required
        self.mean_interarrival = value1
        self.mean_service = value2
        self.num_delays_required = value3



    #get functions to return the value of the attribute num_custs_delayed
    def get_num_custs_delayed(self):
        return self.num_custs_delayed
    #get function to return the value of the attribute num_delays_required
    def get_num_delays_required(self):
        return self.num_delays_required
    #get function to return the valueof the get_next_event_type attribute
    def get_next_event_type(self):
        return self.next_event_type


    #timing function
    #compares time_next_events and set the next event type equal to the event type whose time occurence is the smallest
    def timing(self, outfile):
        i=1
        min_time_next_event = 1.0e+30
        self.next_event_type = 0
        #determine the event type of the next event to occur
        while(i <= self.num_events):
            if(self.time_next_event[i] < min_time_next_event):
                min_time_next_event = self.time_next_event[i]
                self.next_event_type = i
            i+=1
        #check to see whether the event list is empty
        if(self.next_event_type==0):
            #The event list is empty, so stop the simulation
            outfile.write("\nEvent List is Empty at time %f: "%self.time)
            exit(1)
        #The event list is not empty, so advance the simulation clock
        self.time = min_time_next_event

    #Arrival event function
    def arrive(self, outfile):
        #schedule next arrival
        self.time_next_event[1] = self.time + self.expon(self.mean_interarrival)

        #checking to see whether server is busy
        if (self.server_status == self.BUSY):
            #increment number of customers in the queue since server is BUSY
            self.num_in_q+=1
            #check for an overflow condition
            if (self.num_in_q > self.Q_LIMIT):
                #stop simulation since queue has overflowed
                outfile.write("\nOverflow of the array time_arrival at time: %f"%self.time)
                exit(2)
            #still room in the queue?, store the time of arrival of the arriving customer at the (new)
            #end of time_arrival
            self.time_arrival[self.num_in_q] = self.time
        else:
            #server is IDLE
            delay = 0.0
            self.total_of_delays += delay
            #increment number of customers 'num_custs_delayed' delayed
            self.num_custs_delayed += 1
            #make server BUSY
            self.server_status = self.BUSY
            #schedule departure
            self.time_next_event[2] = self.time + self.expon(self.mean_service)

    #departure event function
    def depart(self):
        i=1
        #check whether the queue is Empty
        if(self.num_in_q == 0):
            #make the server idle, since queue is Empty
            self.server_status = self.IDLE
            self.time_next_event[2]= 1.0e+30
        else:
            #queue not Empty
            #Decrement number of customers in Queue
            self.num_in_q -= 1
            #computing delay of customer beginning service
            delay = self.time - self.time_arrival[1]
            #update total delay accumulator
            self.total_of_delays += delay

            #increment number of customers delayed and schedule departure
            self.num_custs_delayed += 1
            self.time_next_event[2] = self.time + self.expon(self.mean_service)
            #move each customer, if any, up one place
            while(i <= self.num_in_q):
                self.time_arrival[i]=self.time_arrival[i+1]
                i += 1
    #report generator method
    def report(self, outfile):
        #computing and writing estimates of the desired measures of performance
        outfile.write("\n\nAverage delay in Queue: %11.3f minutes\n\n"%(self.total_of_delays/self.num_custs_delayed))
        outfile.write("Average number in Queue: %10.3f\n\n"%(self.area_num_in_q/self.time))
        outfile.write("Server Utilization: %15.3f\n\n"%(self.area_server_status/self.time ))
        outfile.write("Time Simulation Ended: %12.3f" %self.time)
    #updating area accumulators for time-average statistics
    def update_time_avg_stats(self):
        #compute time since last event and update last-event-time marker
        time_since_last_event = self.time - self.time_last_event
        self.time_last_event = self.time
        #Update area under number in queue function
        self.area_num_in_q += (self.num_in_q * time_since_last_event)
        #update area under server-busy indicator function
        self.area_server_status += (self.server_status * time_since_last_event)

    #exponential variate generation function
    def expon(self, mean):
        #generating a random number u(0,1) between 0 and 1
        u = np.random.rand()
        #return an exponential random variate with mean "mean"
        return float(-mean * np.log(u))
