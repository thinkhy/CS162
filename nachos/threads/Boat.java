package nachos.threads;

import nachos.ag.BoatGrader;
import nachos.machine.*;  // for Lib.assertTrue()
    
public class Boat
{
    static BoatGrader bg;
                    
    static final int Oahu = 0;
    static final int Molokai = 1;
    static int place = Oahu; // place is Oahu or Molokai
    static int cntPassengers = 0; 
                            
    static Lock boatLock = new Lock();     // boat holds a lock
    static Condition2 arriveOahu    = new Condition2(boatLock);
    static Condition2 arriveMolokai = new Condition2(boatLock);


    static int OahuChildren = 0;
    static int OahuAdults = 0;
    static int MolokaiChildren = 0;
    static int MolokaiAdults = 0;

    public static void selfTest()
    {
	BoatGrader b = new BoatGrader();
	 
	System.out.println("\n***Testing Boats with only 2 children***");
	begin(0, 3, b);
     
    // System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
    // 	 begin(1, 2, b);
     
    // System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
    //   begin(3, 3, b);
    
    }
     
    public static void begin( int adults, int children, BoatGrader b)
    {

	// Store the externally generated autograder in a class
	// variable to be accessible by children.
	bg = b;


    OahuChildren = children;
    OahuAdults = adults;

	// Instantiate global variables here
	
	// Create threads here. See section 3.4 of the Nachos for Java
	// Walkthrough linked from the projects page.
    Runnable r_child = new Runnable() {
        public void run() {
            ChildItinerary();
        };
    };

    Runnable r_adult = new Runnable() {
        public void run() {
            AdultItinerary();
        };
    };
     
    for (int i = 0; i < children; i++) {
        KThread t = new KThread(r_child);
        t.setName("Boat Thread - Child - #" + (i+1));

        System.out.println("Fork Child ");
        t.fork();
        t.join();
    }
     
    for (int i = 0; i < adults; i++) {
        KThread t = new KThread(r_adult);
        t.setName("Boat Thread - Adult - #" + (i+1));
        t.fork();
    }
        
        
    }

    static void ChildItinerary()
    {
        System.out.println("***** ChildItinerary, place: " + place);
        /* This is where you should put your solutions. Make calls
           to the BoatGrader to show that it is synchronized. For
           example:
               bg.AdultRowToMolokai();
           indicates that an adult has rowed the boat across to Molokai
        */

        while (true) {
            if (place == Oahu)
            {
               boatLock.acquire(); 

               if (OahuChildren == 0 && OahuAdults == 0) 
               {  
                   bg.ChildRideToMolokai();
                   MolokaiChildren++;
                   place = Molokai;

                   System.out.println("\n***[Game Over]***");
                   break;
               }

               // wait until available seat on boat
               while (cntPassengers > 2) 
               { 
                   arriveOahu.sleep();
               }

               // book the seat on boat
               cntPassengers++;

               System.out.println("cntPassengers:"+cntPassengers+"\n");

               // two children on boat, the second children ride to Molokai
               if (cntPassengers == 2) 
               {  
                    bg.ChildRideToMolokai();
                    OahuChildren--;

                    MolokaiChildren += 2;
                    cntPassengers = 0;

                    // Children arrive in Molokai, wake up all persons on Molokai
                    place = Molokai; 
                    arriveMolokai.wakeAll();
               }
               // the first passenger(pilot) rows to Molokai
               else if (cntPassengers == 1) 
               { 
                    bg.ChildRowToMolokai();
                    OahuChildren--;
               }

               boatLock.release(); 
            }
            else if (place == Molokai) 
            {
               boatLock.acquire(); 

               Lib.assertTrue(MolokaiChildren > 0);

               // note, just need one child pilot back to Oahu
               bg.ChildRowToOahu();
               arriveOahu.wakeAll();

               MolokaiChildren--;
               OahuChildren++;
               
               place = Oahu; 
               cntPassengers = 0;

               arriveOahu.sleep();
               boatLock.release(); 
            }
        } // while (1);
    }

    static void AdultItinerary()
    {
        
    }

    static void SampleItinerary()
    {
        // Please note that this isn't a valid solution (you can't fit
        // all of them on the boat). Please also note that you may not
        // have a single thread calculate a solution and then just play
        // it back at the autograder -- you will be caught.
        System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
        bg.AdultRowToMolokai();
        bg.ChildRideToMolokai();
        bg.AdultRideToMolokai();
        bg.ChildRideToMolokai();
    }

}


