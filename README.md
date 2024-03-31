# ScalaOnMobile---Example-App


## Overview

This project explores the migration of Scala applications to mobile platforms, inspired by and building upon the foundational work of [Scala on Android](https://github.com/makingthematrix/scalaonandroid). It serves as a demonstrative example, showcasing the integration of native services via [Gluon Attach](https://gluonhq.com/products/mobile/attach/) without the need for the Gluon Charm Glisten library. The application is developed entirely using free projects offered by Gluon and is available in two versions: Scala and Java, for comparative analysis.

## Features

The application offers two main functionalities:

1. **Acceleration Data Visualization:**
   - Reads and displays acceleration data in a graph.
   - Allows exporting the graph as a screenshot and the data as CSV files.

2. **Audio Signal Generation:**
   - Generates Continuous Wave or Chirp signals.
   - Plays the audio within the app.
   - Saves the audio file in an external directory managed by Gluon.

## Implementation

The project contains two main directories, each hosting a version of the application:

- `Scala_App`: The Scala implementation.
- `Java_App`: The Java implementation.

For Android users, pre-packaged APK files are available, facilitating easy installation and testing. Additionally, the Scala application includes a package for installation on iOS devices, showcasing cross-platform capabilities.

## Getting Started

### Prerequisites

- Install Java JDK 11 or later.
- Install [Scala](https://www.scala-lang.org/download/) and [sbt](https://www.scala-sbt.org/download.html) for Scala development.
- Install [Android Studio](https://developer.android.com/studio) for Android app development and testing.
- For iOS deployment, macOS with Xcode is required.

### Installation

1. Clone the repository:
   git clone https://github.com/thomas7b/ScalaOnMobile---Example-App.git
   
3. Navigate to either the `Scala_App` or `Java_App` directory, depending on your development preference.

4. Follow the build and run instructions specific to your platform (Android/iOS).

## Contribution

Contributions to the project are welcome. Please follow the standard fork and pull request workflow. If you have ideas for features or improvements, feel free to open an issue or submit a pull request.


   
