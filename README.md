# README: ImageEnhancer 

**ImageEnhancerExample**, version created by *Marcus Nagy* for course *ET2584* at **BTH**.


# How-to-Use

At load-up there is one button available named **Load new image**, this button allows the user to selected an image from the file storage on the device. After selecting image 3 new things appear.

 - **Radio Group**: 
 `Test Enhancer` - Choose this to prompt for 4 different actions of image enhancing. 
 `V-Transform` - Choose this for using the V-Transform to enhance the image; when selected and click on button *Modify image* the user will be prompted for selecting number of segments. 
 `Smart Enhance` - Choose this to enhance the image in a smart way, where the best value in *Median Absolute Deviation* for different segmentation values of the V-Transform will be returned.
 >**Note:** `Smart Enhance` *requires* substantial computing power and could slow down your device, recommended to be used on newer devices only.
 - **Modify Image**: This button on click might prompt the user for more input data, depending on what enhancer is selected from the *Radio Group*. Once the enhancing is executing there will be a `progressDialog` displayed giving feedback for the progress.
 - **Save**: Button will prompt the user for text input that will require the *name* for the enhanced image being saved to *pictures* on the device.
 > **Note:** The button **Save** might only work properly on API 29 or above, and will display a `Toast` if you're not able to use the function.
 - **Info**: Button will display informative information about segmentation by creating a `Toast`. If no method using segmentation is used a message saying "Nothing to display" will show.
 > **Note:** This information can be modified to display other things for other image enhancing methods.

To **Hide/Show** the options and buttons in the bottom "tap" the `afterImageView`.

To **Hide/Show** the *Load new image* button '"tap" the `beforeImageView`

>**Note:** Using any of the available image enhancing methods  will make a `Toast` appear when *Info* is clicked, displaying information according to the function selected. When `V-Transform` is selected *number of segments* and the *actual number of segments* calculated by the algorithm will be displayed as "Chosen:" and "Actual:", which finds the closest factor to the selected number of segments; `Smart Enhance` will display the best number of segments. Lastly if nothing has been explicitly modified and any other image enhancing method is selected the *Info* button when clicked will tell us "Nothing to display" 

# Version changes

New variables found in the `MainActivity.java` will have a comment in the style of `//NEW: (details)`. All new methods deemed necessary for explanation will have comments above them in the style of `/**     */` . 

*Information about new classes and their function can be found in next section.

## New classes
There is a couple new classes to allow for *V-transform* and *Smart Enhance*.

- **Pixel:** Class is a representation of a pixel with x- and y-cords and a value. Implements `Comparable<Pixel>` and allows effective sorting on the `Pixel` value. Class also uses `Factory Pattern` for easy way of creation, since many pixels are often created.
- ***TemplateEnhancer:*** Abstract class that contains logic/methods which both of the *V-Transform* and the *Smart Enhance* uses. Implements `ImageEnhancer` and is constructed after `Template method Pattern` to be a *template* for both *V-Transform* and *Smart Enhance*.
- **VEnhancer:** Class extends `TemplateEnhancer` and performs the *V-Transform* on a given image with specified number of segments.
- **SmartVEnhancer:** Class extends `TemplateEnhancer` and performs the *V-Transform* on all possible factors but returns the image which produces the best *Median Absolute Deviation* value (which is the value closest to 0.25)

## Changes from Feedback
There have been some minor changes to improve layout for multiple devices by adding an **Info** button and removing a `textField`. This allows for older APIs to be used. **Save** button now works properly but also with improved feedback to the user.

>**Note:** Changes are visible in How-to-Use and everything old has been removed.



