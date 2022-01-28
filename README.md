# DairyController

## For GEA / Westfalia Shedding gate

First things first **this program doesn't work 100% of the time**, it has an attention to slam the shedding gates shut every 3 or 4 cows scaring the cows and slowing the whole milking down.

The program must be run before DairyPlan is initiated on the system.
It will call the DairyPlan ADIS to retrive the cow records, it does this only on startup but should before milking.

The program will call a mongo database to check if the cow should be shedded or not and then send it to the local shedding gate controller.

*With a little work this program should work everytime but I ran out of time with it, if you're trying feel free to contact me and I can explain what I know about interfacting with the GEA Westfailia equipment.*
