---
applyTo: '**'
---
Your app will welcome the user with a login interface. After entering the username and 
password pair correctly your application will close the login interface and will open a new interface 
based on the role of authenticated user such as customer, carrier, and owner. If the user fails 
authentication, your app will warn the user. Users can register themselves as a customer with this 
interface.  During registration, collect necessary user details, such as username, password, and any 
other relevant information. Include validation checks for unique usernames and strong password 
requirements. Store user data in the database. Allow customers to edit their profile information (e.g., 
address, contact details). 
In the customer interface, the user sees his/her username at the appropriate corner of the 
scene (top left, top right, bottom left, or bottom right) and the stage’s title as “GroupXX GreenGrocer”. 
The user can view available vegetables and fruits under different TitledPanes. These products are 
listed with name, image, and price. The lists must be sorted by product name. Note that there will be 
12 vegetables and 12 fruits at least in the beginning of the project. The customer can enter the 
amount in terms of kilograms (0.5 kg, 1 kg, 2.25 kg, etc) for the requested product and add this item 
to the shopping cart if available in stock. If stock is not enough, your app will warn the user about 
stock size. If the user wishes, he may filter the products by a keyword, and the quick search bar can 
be used to efficiently locate a specific product. Note that the shopping cart will be on a different stage 
(interface). The shopping cart will list the added items’ names, amounts, and prices with the total 
cost including taxes (VAT). The user can add/remove more items to the shopping cart or conclude 
shopping by selecting the requested delivery date and time (the user must select a delivery date 
within 48 hours at most after purchasing). Provide a summary of the order before finalizing the 
purchase. To complete a purchase, the customer must meet the minimum cart value requirement. If 
the customer possesses a discount coupon earned from previous purchases, it shall be applied to 
1 
the total amount. Additionally, the customer may be eligible for a loyalty discount based on their past 
completed transactions, and the conditions under which this discount is earned and applied must be 
explicitly stated. When the purchase operation is completed all related information will be 
updated/inserted on related tables in the database. The invoice must be stored in the database in 
PDF format and must also be shared with the customer. The user can also view his/her deliveries, 
and log out via this interface. Additionally, allow customers to cancel orders within a certain time 
frame and provide a history of past orders and delivery status. Customers may evaluate the carrier 
based on a rating system and may communicate with the owner through the application’s messaging 
feature. 
In the carrier interface, the carrier can view deliveries/orders in three different areas such as 
completed, available, current/selected. Available deliveries display the order details such as order 
ID, product list, customer name, customer address, total including V.A.T, requested delivery date... 
Carriers can select any (one or many) available orders. Selected orders will be transferred to the 
current/selected area. After the carrier delivers the products and gets the money, completes the 
order by entering the delivery date. 
In the owner interface, the owner can: 
 add/remove/update products with related attributes (price, name, threshold,...), 
 employ/fire carrier, 
 view all orders (all types), 
 view/reply the customer messages, 
 set / adjust coupons, loyalty standards, 
 view carriers ratings, 
 view reports as charts based on product/time/money... 
The threshold amount defines when the given prices will be doubled! For example, a 
threshold of 5kg for potatoes means that the customer will see the doubled price if the current stock 
of potatoes is equal to or less than 5kg. Yes, the owner is greedy  
Images related to the products should be stored in the database as Binary Large Objects 
(BLOBs). Transaction logs/invoices should be stored in the database as Character Large Objects 
(CLOBs). 

This work is not homework, it is a project. Comprehensive study and research will be your 
priority. You will use SceneBuilder to form different .fxml files for GUIs. You will use MySql as your 
database. Your application will work on a local network!. The initial size of the GUIs will be 960x540 
pixels, centered on the screen. When you maximize the app window, the scene and nodes will 
increase the size respectively. The Back-end and Front-end source codes must be separated like in 
the examples (controllers are important) we reviewed in the lectures. There will be at least six 
different event handlers in this project.

There is no limit on the count of classes, methods, and attributes in your application. Higher 
is better. You will use the database-adapter class to operate on the database. 
Please make sure to use Object-Oriented Design principles while creating classes and other 
components. This project encourages the application of encapsulation, inheritance, and 
polymorphism, promoting a structured and efficient system design. 
Please make sure your code runs without errors when you submit your projects. 
Some logical errors which are not handled (with try-catch blocks or other techniques) such as: 
 Entering zero or negative amounts in customer GUI for product amount, 
 Entering non-double value (can be char, string, etc.) in customer GUI for product amount, 
 Displaying products with zero stock in customer GUI, 
 Not merging the same product in the shopping cart (When the customer added 1.25 kg 
tomatoes, then added 0.75 kg tomatoes in the same order, the customer will view tomatoes 
2 kg as merged in the shopping cart, not separate!) 
 Selecting the order selected by different carriers in carrier GUI, 
 Threshold values are not working,  
 Entering zero or negative threshold value for a product in owner GUI... 
can cause early termination of your presentation and demo! 