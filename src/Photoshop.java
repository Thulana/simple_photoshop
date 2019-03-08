

/** 
 *
 * @author thulana
 * @version 1.0.1
 * @since 2018-11-21
 */
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Photoshop extends Application {

    private int[][] laplasian_filter = {{-4, -1, 0, -1, -4}, {-1, 2, 3, 2, -1}, {0, 3, 4, 3, 0}, {-1, 2, 3, 2, -1}, {-4, -1, 0, -1, -4}};

    @Override
    public void start(Stage stage) throws FileNotFoundException {
        stage.setTitle("Photoshop");

        //Read the image
        Image image = new Image(new FileInputStream("src/raytrace.jpg"));

        //Create the graphical view of the image
        ImageView imageView = new ImageView(image);

        //Create the simple GUI
        Label input_label = new Label("Input ");
        TextField gamma_textField = new TextField("");
        Button invert_button = new Button("Invert");
        Button gamma_button = new Button("Gamma Correct");
        Button contrast_button = new Button("Contrast Stretching");
        Button histogram_button = new Button("Histograms");
        Button cc_button = new Button("Cross Correlation");
        Button reset_button = new Button("Reset");

        //Add all the event handlers (this is a minimal GUI - you may try to do better)
        invert_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Invert");
                //At this point, "image" will be the original image
                //imageView is the graphical representation of an image
                //imageView.getImage() is the currently displayed image

                //Let's invert the currently displayed image by calling the invert function later in the code
                Image inverted_image = ImageInverter(imageView.getImage());
                //Update the GUI so the new image is displayed
                imageView.setImage(inverted_image);
            }
        });

        gamma_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Gamma Correction");
                try {
                    Image gamma_correction = GammaCorrection(imageView.getImage(), Double.parseDouble(gamma_textField.getText()));
                    imageView.setImage(gamma_correction);
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("Please enter gamma value");
                    alert.showAndWait();

                }

            }
        });

        reset_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("RESET");

                imageView.setImage(image);
            }
        });

        contrast_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Contrast Stretching");
                try {
                    String[] data = gamma_textField.getText().split(",");
                    Image contrast_stretch = contrast_stretch(imageView.getImage(), Integer.parseInt(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3]));
                    imageView.setImage(contrast_stretch);
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("please enter r1,r2,s1,s2 values in the order ");
                    alert.showAndWait();

                }

            }
        });

        histogram_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Histogram");
                Image histogram = histogram(imageView.getImage());
                imageView.setImage(histogram);

            }
        });

        cc_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Cross Correlation");
                Image filtered = applyFilter(imageView.getImage(), laplasian_filter);
                imageView.setImage(filtered);
//                System.out.println((int)(5/2));
            }
        });

        //Using a flow pane
        FlowPane root = new FlowPane();
        //Gaps between buttons
        root.setVgap(10);
        root.setHgap(5);

        //Add all the buttons and the image for the GUI
        root.getChildren().addAll(input_label, gamma_textField, invert_button, gamma_button, contrast_button, histogram_button, cc_button, reset_button, imageView);

        //Display to user
        Scene scene = new Scene(root, 1024, 768);
        stage.setScene(scene);
        stage.show();
    }
    // general method to apply given filter in this case cross correlation using gaussian
    public Image applyFilter(Image image, int[][] filter) {
        //Find the width and height of the image to be process
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        //Create a new image of that width and height
        WritableImage filtered = new WritableImage(width, height);
        //Get an interface to write to that image memory
        PixelWriter filtered_writer = filtered.getPixelWriter();
        //Get an interface to read from the original image passed as the parameter to the function
        PixelReader image_reader = image.getPixelReader();
        int filter_size = filter.length / 2;
        //Iterate over all pixels
        double redmin = Double.POSITIVE_INFINITY;
        double redmax = Double.NEGATIVE_INFINITY;
        double bluemin = Double.POSITIVE_INFINITY;
        double bluemax = Double.NEGATIVE_INFINITY;
        double greenmin = Double.POSITIVE_INFINITY;
        double greenmax = Double.NEGATIVE_INFINITY;
        System.out.println(filter.length);
        // cross correlated value raster
        double[][][] imageRaster = new double[height][width][3];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double [] color = new double[3];
                if (x < filter_size || y < filter_size) {
                    color[0] = image_reader.getColor(x, y).getRed();
                    color[1] = image_reader.getColor(x, y).getBlue();
                    color[2] = image_reader.getColor(x, y).getGreen();
                } else if (height - filter_size - 1 < y || width - filter_size - 1 < x) {
                    color[0] = image_reader.getColor(x, y).getRed();
                    color[1] = image_reader.getColor(x, y).getBlue();
                    color[2] = image_reader.getColor(x, y).getGreen();
                } else {
                    color[0] = image_reader.getColor(x, y).getRed();
                    color[1] = image_reader.getColor(x, y).getBlue();
                    color[2] = image_reader.getColor(x, y).getGreen();
                    double red = 0;
                    double blue = 0;
                    double green = 0;
                    
                    for (int i = 0; i < filter.length; i++) {
                        for (int j = 0; j < filter.length; j++) {
                            red += filter[i][j] * image_reader.getColor(x-2+j , y-2+i ).getRed();
                            blue += filter[i][j] * image_reader.getColor(x-2+j , y-2+i ).getBlue();
                            green += filter[i][j] * image_reader.getColor(x-2+j, y-2+i ).getGreen();
                        }
                    }
                    color[0] = red;
                    color[1] = blue;
                    color[2] = green;
                 
                }
                if (redmin > color[0]) {
                    redmin =  color[0];
                }
                if (redmax < color[0]) {
                    redmax = color[0];
                }
                if (bluemin > color[1]) {
                    bluemin = color[1];
                }
                if (bluemax < color[1]) {
                    bluemax = color[1];
                }
                if (greenmin > color[2]) {
                    greenmin = color[2];
                }
                if (greenmax < color[2]) {
                    greenmax = color[2];
                }

                imageRaster[y][x][0] = color[0];
                imageRaster[y][x][1] = color[1];
                imageRaster[y][x][2] = color[2];
                //Apply the new colour
//                filtered_writer.setColor(x, y, color);
            }
        }

     //    Normalize the values using min max values
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                double red = (imageRaster[i][j][0] - redmin) / (redmax - redmin);
//                System.out.println(imageRaster[i][j].getRed()+" - "+redmax+" - "+redmin+" - "+red);
                double blue = (imageRaster[i][j][1] - bluemin) / (bluemax - bluemin);
                double green = (imageRaster[i][j][2] - greenmin) / (greenmax - greenmin);
                filtered_writer.setColor(j, i, Color.color(red, green, blue));
            }
        }
        return filtered;
    }

    //Example function of invert
    public Image ImageInverter(Image image) {
        //Find the width and height of the image to be process
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        //Create a new image of that width and height
        WritableImage inverted_image = new WritableImage(width, height);
        //Get an interface to write to that image memory
        PixelWriter inverted_image_writer = inverted_image.getPixelWriter();
        //Get an interface to read from the original image passed as the parameter to the function
        PixelReader image_reader = image.getPixelReader();

        //Iterate over all pixels
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                //For each pixel, get the colour
                Color color = image_reader.getColor(x, y);
                //Do something (in this case invert) - the getColor function returns colours as 0..1 doubles (we could multiply by 255 if we want 0-255 colours)
                color = Color.color(1.0 - color.getRed(), 1.0 - color.getGreen(), 1.0 - color.getBlue());
                //Note: for gamma correction you may not need the divide by 255 since getColor already returns 0-1, nor may you need multiply by 255 since the Color.color function consumes 0-1 doubles.

                //Apply the new colour
                inverted_image_writer.setColor(x, y, color);
            }
        }
        return inverted_image;
    }

    //Example function of gamma correction
    public Image GammaCorrection(Image image, double gamma) {
        //Find the width and height of the image to be process
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        double new_gamma = 1.0 / gamma;

        double[] gamma_ratio = new double[256];

        for (int i = 0; i < gamma_ratio.length; ++i) {
            double val = Math.pow(((double) i / 255), (new_gamma));
            gamma_ratio[i] = val;
        };

        //Create a new image of that width and height
        WritableImage gamma_correction = new WritableImage(width, height);
        //Get an interface to write to that image memory
        PixelWriter gamma_correction_writer = gamma_correction.getPixelWriter();
        //Get an interface to read from the original image passed as the parameter to the function
        PixelReader image_reader = image.getPixelReader();

        //Iterate over all pixels
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                //For each pixel, get the colour
                Color color = image_reader.getColor(x, y);
                //Do something (in this case invert) - the getColor function returns colours as 0..1 doubles (we could multiply by 255 if we want 0-255 colours)
                color = Color.color(gamma_ratio[(int) (color.getRed() * 255)], gamma_ratio[(int) (color.getGreen() * 255)], gamma_ratio[(int) (color.getBlue() * 255)]);
                //Note: for gamma correction you may not need the divide by 255 since getColor already returns 0-1, nor may you need multiply by 255 since the Color.color function consumes 0-1 doubles.

                //Apply the new colour
                gamma_correction_writer.setColor(x, y, color);
            }
        }

        return gamma_correction;
    }

    // function to draw hisotgrams and do the normalization
    public Image histogram(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        PixelReader image_reader = image.getPixelReader();
        // create histogram array for each color and brightness
        int[] redHisto = new int[256];
        int[] blueHisto = new int[256];
        int[] greenHisto = new int[256];
        int[] brightHisto = new int[256];
        int[] greyHisto = new int[256];
        // loop over pixels and collect histogram data
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                //For each pixel, get the colour
                Color color = image_reader.getColor(x, y);
                int red = (int) Math.round(color.getRed() * 255);
                redHisto[red] += 1;
                int blue = (int) Math.round(color.getBlue() * 255);
                blueHisto[blue] += 1;
                int green = (int) Math.round(color.getGreen() * 255);
                greenHisto[green] += 1;
                int bright = (int) Math.round(color.getBrightness() * 255);
                brightHisto[bright] += 1;
                
                
                int grey = (red+blue+green)/3;
                greyHisto[grey] +=1;

            }
        }
        ArrayList<int[]> imageLUT = new ArrayList<int[]>();

        // draw the hisotgram using collected array
        drawHistogram("Red Histogram", redHisto);
        drawHistogram("Blue Histogram", blueHisto);
        drawHistogram("Green Histogram", greenHisto);
        drawHistogram("Brightness Histogram", brightHisto);

        // creating a lookup table with accumulated intensity histogram values

        int[] greyLUT = new int[256];

        int greyC = 0; 
        for (int i = 0; i < 256; i++) {

              greyC += greyHisto[i];
              greyLUT[i] = greyC;

        }

        drawHistogram("Accumulated Histogram", greyLUT);
        //Create a new image of that width and height
        WritableImage histo_eql = new WritableImage(width, height);
        //Get an interface to write to that image memory
        PixelWriter histo_eql_writer = histo_eql.getPixelWriter();

        // compute equalized intensity values
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                //For each pixel, get the colour
                Color color = image_reader.getColor(x, y);
                double val = (((greyLUT[(int) Math.round(color.getRed() * 255)] - greyLUT[0]) / ((image.getWidth() * image.getHeight()) - greyLUT[0])) * (256 - 1)) / 255;
                Color new_color = Color.color(val, val, val);
                //Apply the new colour
                histo_eql_writer.setColor(x, y, new_color);
            }
        }
        return histo_eql;
    }

    public void drawHistogram(String name, int[] data) {
        //Defining the x axis             
        NumberAxis xAxis = new NumberAxis(0, 256, 1);
        xAxis.setLabel("Intensity");

        //Defining the y axis   
        NumberAxis yAxis = new NumberAxis(0, Arrays.stream(data).max().getAsInt(), 10000);
        yAxis.setLabel("Frequency");

        //Creating the line chart 
        LineChart linechart = new LineChart(xAxis, yAxis);

        //Prepare XYChart.Series objects by setting data 
        XYChart.Series series = new XYChart.Series();
        series.setName(name);

        for (int i = 0; i < data.length; i++) {
            series.getData().add(new XYChart.Data(i, data[i]));
        }

        //Setting the data to Line chart    
        linechart.getData().add(series);

        //Creating a Group object  
        Group root = new Group(linechart);

        //Creating a scene object 
        Scene scene = new Scene(root, 600, 400);
        Stage stage = new Stage();
        //Setting title to the Stage 
        stage.setTitle("Histogram");

        //Adding scene to the stage 
        stage.setScene(scene);

        //Displaying the contents of the stage 
        stage.show();
    }

    
    public Image contrast_stretch(Image image, int r1, int r2, int s1, int s2) {
        //Find the width and height of the image to be process
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        //Create a new image of that width and height
        WritableImage contrast_stretch = new WritableImage(width, height);
        //Get an interface to write to that image memory
        PixelWriter contrast_stretch_writer = contrast_stretch.getPixelWriter();
        //Get an interface to read from the original image passed as the parameter to the function
        PixelReader image_reader = image.getPixelReader();

        //Iterate over all pixels;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                //For each pixel, get the colour
                Color color = image_reader.getColor(x, y);

                double red = color.getRed();
                double blue = color.getBlue();
                double green = color.getGreen();
                // stretch the colors and recreate the pixel
                Color new_color = Color.color(stretch(red, r1, r2, s1, s2), stretch(green, r1, r2, s1, s2), stretch(blue, r1, r2, s1, s2));
                //Note: for gamma correction you may not need the divide by 255 since getColor already returns 0-1, nor may you need multiply by 255 since the Color.color function consumes 0-1 doubles.

                //Apply the new colour
                contrast_stretch_writer.setColor(x, y, new_color);
            }
        }
        return contrast_stretch;
    }
    
    // function to calculate stretched values
    public double stretch(double val, int r1, int r2, int s1, int s2) {
        if (val * 255 < r1) {
            return (s1 / r1) * val;
        } else if (r1 <= val * 255 && val * 255 <= r2) {
            return ((((s2 - s1) / (r2 - r1)) * (val * 255 - r1)) + s1) / 255;
        } else {
//            System.out.println("working");
            return ((((255 - s2) / (255 - r2)) * (val * 255 - r2)) + s2) / 255;
        }
    }

    public static void main(String[] args) {
        launch();
    }

}
