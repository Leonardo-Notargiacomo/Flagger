# Test Scenarios: Mark a place

## Test Scenario 1: The place ,in question, is Fontys Venlo

**Preconditions**:  
- The user is logged in.  
- The user allowed the usage of their location.

**Steps**:  
1. User, standing in the 1.93 room in Fontys Venlo, attempts to mark the place.  
2. The system finds the place and its properties, stores the relevant data (userId, place_id (string, which describes Fontys Venlo), date_taken)  and displays the camera interface.
3. User takes a picture 
4. System saves the user flag data and displays the map with the new flag.  


**Expected Result**:  
The place is marked as visited for this user

---

## Test Scenario 2: The place ,in question, is described with coordinates this way 4°38'26.7"S 1°42'16.1"W


**Preconditions**:  
- The user is logged in.  
- The user allowed the usage of their location.

**Steps**:  
1. User, swimming in the middle of the Atlantic ocean, attempts to mark the place.  
2. The system does not find the place and its properties, displaying  a fail message .  


**Expected Result**:  
The place is not marked as visited for this user
---

## Test Scenario 3:  The place ,in question, is Fontys Venlo, the camera 

**Preconditions**:  
- The user is logged in.  
- The user allowed the usage of their location.

**Steps**:  
1. User, standing in the 1.93 room in Fontys Venlo, attempts to mark the place.  
2. The system finds the place and its properties, stores the relevant data (userId, place_id (string, which describes Fontys Venlo), date_taken)  and displays the camera interface.
3. User does not take the picture and exits the app.



**Expected Result**:  
The place is not marked as visited for this user
---