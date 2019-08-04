# Steps On How To Run The JAR.

### Prerequisites
* Java 8 or above must be installed
* Xampp or Wampp must be installed and Running
* Create a database with name _usersprints_
* Create a new user in _"localhost/phpmyadmin"_ with User name: _"krise"_, Host: _"local"_, Password: _"sKndF5xMJpE8Ayp3"_
* Grant the user all privileges on wildcards and on global
##

### Guides To Run JAR

* Start up the application(jar) on your command prompt with: _"java -jar distributed-batch-processing-0.0.1-SNAPSHOT.jar"_

##
* The app would start on port _5070_
#
* Visit _"localhost:5070/swagger-ui.html"_ to read the api documentation, lookup for the data persisted in _"localhost/phpmyadmin"_, database: _"usersprints"_
#
* USE _"localhost:5070/fingerprint/connect/reader"_ to try and connect to any connected U.are.U finger print scanner
    * This returns _TRUE_ if successful and _FALSE_ otherwise
#
* USE _"localhost:5070/fingerprint/scan/{String fingerToScan}"_ to try and connect to any connected U.are.U finger print scanner and scan a finger
    * This takes a String (name of the finger to be scanned) in the URL, hence replace {String fingerToScan} with names like "right-thumb"
    * This returns a _BASE64 ENCODED STRING_ if successful and _NULL_ otherwise
    * NB: The returned base64 string can be converted to an image and displayed if needed, for web developers, checkout javascript function that does this.
#
* USE _"localhost:5070/fingerprint/create/{userUniqueId}"_ to try and connect to any connected U.are.U finger print scanner, scan and save a new user with their prints
      * This takes a String (users unique identity, can be id or email or any unique identifier) in the URL, hence replace {userUniqueId} with a unique identifier e.g "2009945HH"
      * NB: This link takes a request body in form of json, the json request looks like 
      {
            "rightThumbString":"_A BASE64 STRING_" (this base64 string is the string gotten when an image is encoded in base64),
            "leftThumbString":"_A BASE64 STRING_" (this base64 string is the string gotten when an image is encoded in base64)
      }.
      * This returns a String to tell if the operation was successful or not
#
