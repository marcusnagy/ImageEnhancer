# README: ImageEnhancer 

**ImageEnhancerExample**, version created by *Marcus Nagy* for course *ET2584* at **BTH**.


# How-to-Use

At load-up there is one button available named **Load new image**, this button allows the user to selected an image from the file storage on the device. After selecting image 3 new things appear.

 - **Radio Group**: 
 `Test Enhancer` - Choose this to prompt for 4 different actions of image enhancing. 
 `V-Transform` - Choose this for using the V-Transform to enhance the image; when selected and click on button *Modify image* the user will be prompted for selecting number of segments. 
 `Smart Enhance` - Choose this to enhance the image in a smart way, where the best value in *Median Absolute Deviation* for different segmentation values of the V-Transform will be returned.
 - **Modify Image**: This button on click might prompt the user for more input data, depending on what enhancer is selected from the *Radio Group*. Once the enhancing is executing there will be a `progressDialog` displayed giving feedback for the progress.
 - **Save**: Button will prompt the user for text input that will require the *name* for the enhanced image being saved to *pictures* on the device.
 > **Note:** The button **Save** might only work properly on API 29 or above, and will only execute if there is an enhanced image in `afterImageView`.

To **Hide/Show** the options, segmentation info and buttons in the bottom "tap" the `afterImageView`.

To **Hide/Show** the *Load new image* button '"tap" the `beforeImageView`

>**Note:** Using either *V-Transform* or *Smart Enhance* will make a `textView` appear showing what the user selected for *number of segments* and the *actual number of segments* calculated by the algorithm, which finds the closest factor to the desired input; respectively *Smart Enhance* will display the best number of segments.
>
>To display the segmentation info API 30 or above is **required**.

# Version changes

New variables found in the `MainActivity.java` will have a comment in the style of `//NEW: (details)`. All new methods deemed necessary for explanation will have comments above them in the style of `/**     */` . 

*Information about new classes and their function can be found in next section.

## New classes
There is a couple new classes to allow for *V-transform* and *Smart Enhance*.

- **Pixel:** Class is a representation of a pixel with x- and y-cords and a value. Implements `Comparable<Pixel>` and allows effective sorting on the `Pixel` value. Class also uses `Factory Pattern` for easy way of creation, since many pixels are often created.
- ***TemplateEnhancer:*** Abstract class that contains logic/methods which both of the *V-Transform* and the *Smart Enhance* uses. Implements `ImageEnhancer` and is constructed after `Template method Pattern` to be a *template* for both *V-Transform* and *Smart Enhance*.
- **VEnhancer:** Class extends `TemplateEnhancer` and performs the *V-Transform* on a given image with specified number of segments.
- **SmartVEnhancer:** Class extends `TemplateEnhancer` and performs the *V-Transform* on all possible factors but returns the image which produces the best *Median Absolute Deviation* value (which is the value closest to 0.25)

