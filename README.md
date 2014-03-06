Team39
======

SimCity201 Project Repository for CS 201 students

Team Assignments
================

	Michael:

		-A* and the entire movement manager
		-thread safe messages 
		-Person agent
		-Puppet agent
		-Housing agents and gui
		-cars
		-People integration into cityGui and interacting with buildings
		-helping everyone on the team survive this project
	
	Janet:
		-city gui
		-drawing the city background
		-panels and gui that the user will interact with
		-building integration into cityGui
		-bus agent
		-trace panel
		-design docs for bus agent
	
	Jacob:
		-Entire restaurant arena, including design docs and inside gui
		-All Restaurant Unit Tests
		-Code to force shutdown buildings
		-Apartments, including design docs and inside gui
	
	Sean:
		-Entire market arena
		-Entire gui for the market arena
		-All Market Unit Tests
		-design docs for market arena
	
	Andrew:
		-Entire bank arena
		-Entire gui for bank arena
		-bank background, market background, restaurant background
		-All Bank Unit Tests
		-bank robber
		-design docs for bank arena and person arena 
		-readMe :D
	

User Instructions
=================

Welcome to District 39! Follow these step by step instructions for running CityGui.gui

May the odds be ever in your favor.


Adding People
-------------

-On the left side of screen, you will see the person creation panel. 
-Type in a person's name and the amount of money they have on them. 
-Then select whether they live in and apartment or a house, and their mode of transportation.
-Last, decide the person's role inside the city. 
-Upon clicking add person, your newly created person will be added to the city and a bank account will be automatically set up at the local bank.

-Or you can click the 'Populate City' button at the button to create 50 people, all with different roles. 


More Interaction Panels
-----------------------

-Next to the person creation panel is a list. If you click a person, they will turn red while walking around the big map.
-Also when you click on a person, the information panel will show their name, work place, and job.
-Below the person creation panel is the current date and time, with 0-0-0 (night time) being when you started running the application.
-Below the date and time is slider for setting the speed of the city, with the left being the slowest speed and the right being the fastest.


Moving About The City
---------------------

-Clicking on any building (except the bus stops) will take you to the inside view.
-Inside buildings, you can watch citizens move about their daily routines.
-Our city implements 3 time phases, morning work shift, afternoon work shift, and night time.
-Buildings open during the morning shift.
-When the afternoon shift comes around, the morning shift workers are relieved of duty and the afternoon shift workers take over.
-Buildings close during the evening shift.
-Restaurants and Markets are closed during the weekends.
-The bank is open every day of the week.
-There is a button to force shutdown of a building.
			-A building under force shutdown will immediately close
			-A building under force shutdown will not be able to reopen unless told to
			-If a building is told to reopen during the night, it will reopen again during the standard work hours the next day
-The trace log (the second window) keeps track of all the conversations being held in the city.


Homes
-----

-Inside their home, people will sleep, eat, clean, put groceries in their refrigerator and go to the bathroom.



Apartments
----------

-Apartment buildings have 8 individual rooms; each room can hold up to 2 people.
-Inside apartments, people will enter their assigned room, drop off groceries at the fridge, cook a meal, go to the bathroom, and sleep.
-Correctly uses the special log alert system.
-In-isolation trial module available (see below for instructions).


Restaurant
----------

-Restaurants open when there are enough workers hired for that shift to successfully run it -- a host, a cook, a cashier, and at least one waiter.
-Restaurants stay closed on weekends.
-Restaurants can be forced to close and stay closed by clicking a checkbox in the inside-view sidebar. They can be reopened by unchecking.
-If a customer is forced to wait too long, he or she will leave.
-Non-normative behavior supported for customers: food preference, flake, impatient
	-If the Person's name contains the name of a food (case insensitive), he will try to order that food if he sees it
	-If the Person's name contains "flake" (case insensitive), he will order regardless of whether he can afford the food
	-If the Person's name contains "impatient" (case insensitive), he will wait a shorter time for service before leaving
-In the event that a customer leaves out of impatience, any food for him is thrown away and he does not pay any bill he might have generated.
-In the event that a customer underpays, cashier sends a message to the alert log and remembers the customer's debt for his next visit.
-Correctly uses the special log alert system.
-In-isolation trial module available (see below for instructions).

***We made a petition for our group and were informed by Professor Wilczynski that our city only needs one of our restaurants, which Jacob made from scratch for this project***

Bank
----

-Bank tellers assist bank customers.
-Bank customers will either deposit money or withdraw money.
				-If they try to withdraw more money than they have, the teller will refuse their request
-If a bank customer is forced to wait too long, he or she will leave.
-Bank Robbers are crafty thieves!! They break into the bank late at night.
					-Bank robbers will attempt to break into the safe, if they fail they leave the bank
					-If they succeed, the will either make off with 50, 100, 500, or 1000 dollars
			

Buses
-----

-Buses help citizens move around the city.
-Citizens will use bus stops if they are a bus user.


Market
------

-Market offers 34 kinds of foods. The quantity of each item is a random number between 50 and 100 and market can not restock itself 
-Cars are available for purchase from the market.
-People and restaurants can order anything available from the market of arbitrary amount.
-Market has two shifts and closes after the second shift on weekdays and all day on weekends.
-Market can be forced close from the gui.
-If a customer waits too long, he or she will leave the market.
-Customers would also leave if the market closes normally or is forced close.
-Market clerks would go grab items for customers.
-There are 4 locations for foods and one location for car.
-Market can be paused like all other buildings. 




Special Features
================

-Full implementation of A*
-100% thread safe code
-Music
-Enhanced Gui













	