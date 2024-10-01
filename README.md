# Image Conversion Tool

## Description
This is a web-based Image Conversion Tool that allows users to convert, resize, and compress images. It supports multiple file uploads, various image formats, and provides options for image quality and metadata handling.

## Features
- Convert images to different formats (currently supports JPEG and WebP)
- Resize images by specifying width (height is automatically calculated to maintain aspect ratio)
- Compress images with adjustable quality settings
- Strip metadata from images
- Batch processing of multiple images
- Download converted images individually or as a zip file for multiple conversions

## Technologies Used
- Backend: Java with Spring Boot
- Frontend: HTML, CSS, JavaScript
- Build Tool: Maven
- Image Processing: Java ImageIO, Apache Commons Imaging
- Multithreading: Java Concurrent Utilities

## Setup and Installation
1. Ensure you have Java 17 and Maven installed on your system.
2. Clone the repository:
   ```
   git clone [repository-url]
   ```
3. Navigate to the project directory:
   ```
   cd ImageTool
   ```
4. Build the project:
   ```
   mvn clean install
   ```
5. Run the application:
   ```
   java -jar target/ImageTool-0.0.1-SNAPSHOT.jar
   ```
6. Open a web browser and go to `http://localhost:8080` to access the application.

## Usage
1. Select one or multiple image files (up to 50) using the file upload button.
2. Choose the desired output format from the dropdown menu.
3. Adjust the width for each image or use the "Apply to all" checkbox to set a global width.
4. Set the compression quality using the slider.
5. Check the "Strip Metadata" box if you want to remove image metadata.
6. Click the "Convert" button to process the images.
7. Download the converted images individually or as a zip file (for multiple images).

## Configuration
- Maximum file size and request size can be configured in `application.yaml`.
- Thread pool settings for image processing can be adjusted in `ThreadPoolConfig.java`.

## Contributing
Contributions to improve the Image Conversion Tool are welcome. Please follow these steps:
1. Fork the repository
2. Create a new branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License
[Specify your license here]

## Contact
Your Name - [your-email@example.com]

Project Link: [https://github.com/yourusername/ImageTool](https://github.com/yourusername/ImageTool)

## Acknowledgments
- [List any libraries, tools, or resources you've used and want to acknowledge]