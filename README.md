# flexpath-final-project

## Overview

This is the final project for the FlexPath program.  The project is a full-stack application that will allow users to create, curate, and retrieve items and groupings of items.  The application includes a SQL database, a backend RESTful API built with Spring Boot and a frontend application built with React.

The details of which items and groupings are created, curated, and retrieved are up to you to decide, as is the overall look and feel of the application.  You should use the included starter code as a base to build upon, but you are free to modify the code as needed to meet the requirements of the project.  You should choose a project that is interesting to you and that you would feel comfortable discussing in an interview.

This project will require you to use all of the skills you have learned throughout the FlexPath program.


## Project Starter Code

Starter code for this project is included in this repository, in three folders:

- `database` - A SQL file to create the initial database and authentication tables (users/roles).
- `backend` - A Spring Boot application with a RESTful API. Authentication and user/role management is included in the starter code.
- `frontend` - A basic React application.

You will need to build upon this starter code to create your final project.

## Requirements

### Constraints

- You must use the stack that was taught in the FlexPath program (Java, Spring Boot, React, MySQL).
- Your project must be your own work.  You may not use code from other students.
- You may use external resources such as Stack Overflow to troubleshoot and design code. However, you are not allowed to just copy chunks of code into your project without understanding and modifying them. In order to pass, you must be able to articulate your understanding of all the code contained within the project. 
- If you use pieces of code from previous projects in the program, you must modify them to fit your project's design and be able to explain why you used these pieces of code and how the code works.
- AI-based tools such as ChatGPT may be used for learning and understanding code, but not to generate code for the project. Using AI generated code within the project will result in a fail.
- You may not use 3rd party libraries that aren’t included in the starter code or mentioned in this document.

### Application Requirements

#### Creation

- Allow users to create items for public or private consumption.
- Allow users to add, edit and delete their own items.
- Administrators should be able to view, edit and delete all items.

#### Curation

- Allow users to create groupings of items and add items to groups.
- These item groupings should be able to be private or publicly visible to other users.
- Users should be able to create, edit and delete their own groups and items.
- Users should be able to view other users' public items and groups.
- Administrators should be able to view, edit and delete all groups.

#### Retrieval

- Users should be able to easily navigate through their items and groupings, and view other users' public items and groupings.
- Users should be able to search through items and groupings based on at least two query parameters.
  - At least one of these should allow for a LIKE comparison to find relevant results in the database.
- Users should be able to sort lists of groups by at least two fields in either ascending or descending order.
- Users should be able to sort lists of items by at least two fields in either ascending or descending order.

### Backend Spring Boot Requirements

- You must create a RESTful API using Spring Boot that will be used by the frontend application.
- You must create controllers, services and data access classes as needed to create API endpoints that will be called by the frontend application to create, curate, and retrieve items and groupings of items from the database.
- You must use roles-based authorization to control access to different parts of your application API.
- You must implement Unit tests covering at least 50% of your backend Java code.

### Frontend React Application Requirements

- You must create a React application that will interact with the backend API to create, curate, and retrieve items and groupings of items.
- Pages:
  - You must have at least 3 pages in your application.
  - You must use React Router to navigate between pages.
- Components:
  - You must break repeated parts of your UI into components.
- Styling:
  - You must style your application using any combination of the following:
    - Bootstrap
    - Tailwind CSS
    - Custom CSS
  - You *may* include icons with your app using one of the following:
    - The FREE version of Font Awesome version 5: https://fontawesome.com/
    - Bootstrap icons: https://icons.getbootstrap.com/
- Testing:
  - You must implement Unit tests covering at least 50% of your frontend React and JS code.

### Database Requirements

- You must use MySQL as your database.
- You must create tables to store items and groupings of items.
- You must use appropriate data types for each column in your tables.
- You must include primary keys for each table.
- You must include foreign keys where appropriate to enforce data integrity.

### Example of what the items could be to help with project brainstorming

- Blog Posts
- Client Testimonials for Products
- Video Game Reviews
- Restaurant Reviews
- Recipes


## Submission Requirements
When you are ready to submit your assessment, you will submit your 
GitHub Repo link and Walkthrough video .mp4 file through canvas.

Your project code must be able to be pulled down from GitHub and successfully built on a Windows or Mac device. 
PLEASE ENSURE that you include all SQL code that creates the initial models, table relationships, and data for your MySQL database. Course staff will use that SQL to create a MySQL database for your project so we can run your project locally.

If you do not include this in your project, and your app cannot be built and run when pulling down the code from GitHub, we won't be able to schedule your project demo. 

If we get to the end of the course and we can't run your project on our device, and you are unable to get your repo in a working state, **you will be unable to graduate from the course**.

### Requirements for GitHub Repo:
- Once submitted, the application in your GitHub repo must be able to build
and run. 
- Includes necessary SQL files to create the initial Db tables, relationships, and any starter data your app needs
- LaunchCode staff will be pulling down your app to review your code,
ensure that the application has all required features, meets the style
requirements, and can be built and run on their local device. 


### Requirements for Walkthrough Video:

[Link to download OBS](https://obsproject.com/)

Download the right version of OBS for your device (Windows or Mac) from
the home page.
![OBS Homepage](/readMeFiles/obs-homepage.png)

Watch the following tutorial for how to record your screen and capture 
audio from your laptop with OBS:
[Tutorial for how to record your screen with OBS](https://www.youtube.com/watch?v=j1HIHYRnOfo)

Please save your walkthrough video as an `.mp4` file.

We will NOT accept a video shot from your phone of your computer screen.
Please install the OBS application, watch the tutorial video, and record
the video directly on your computer.

Walkthrough Video Requirements:
1. Your video MUST be at least 15 minutes long and NO LONGER than 25 minutes
2. Show yourself starting the application on your device. Then open
your web browser and navigate to the running React app.
3. Give us a walkthrough of the features of the app and how they run
4. Then, show us your code files in VS Code. Give us a quick summary
of how you organized your app and walk us through your most complicated React and Java file in depth 
5. Then, tell us which feature requirements were the most difficult for you
to implement. Tell us why it was difficult and the process you went through
to figure them out.

Video Technical Requirements:
1. We must be able to hear you walking us through everything in the video, so
make sure to capture your laptops microphone audio in OBS.
2. The code file text must be clear and readable in the video. Ensure
you are capturing a good Base Resolution for your video
in the OBS settings. A good Base Resolution is 1920x1080.
![Example of good video recording settings](/readMeFiles/video-recording-settings.png)
3. You can find where OBS is saving your videos inside of Settings -> Output -> Recording:
![Example of where OBS files are saved](/readMeFiles/recording-files-path.png)


# If you encounter any issues during this process, please seek help in this order:
1. Reach out to your Peers in the Slack
2. Log a ticket to the LaunchCode Support Queue: https://support.launchcodelearning.org/support/tickets/new
3. Reach out to Course Staff in the Slack