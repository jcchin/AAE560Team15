
ControlFrame addControlFrame(String theName, int theWidth, int theHeight) {
  Frame f = new Frame(theName);
  ControlFrame p = new ControlFrame(this, theWidth, theHeight);
  f.add(p);
  p.init();
  f.setTitle(theName);
  f.setSize(p.w, p.h);
  f.setLocation(100, 100);
  f.setResizable(false);
  f.setVisible(true);
  return p;
}


// the ControlFrame class extends PApplet, so we 
// are creating a new processing applet inside a
// new frame with a controlP5 object loaded
public class ControlFrame extends PApplet {

  int w, h;

  int abc = 100;
  
  //heat map stuff
  PImage backgroundImage; // background image
  PImage heatmapBrush; // radial gradient used as a brush. Only the blue channel is used.
  PImage heatmapColors; // single line bmp containing the color gradient for the finished heatmap, from cold to hot
  PImage clickmapBrush; // bmp of the little marks used in the clickmap

  PImage gradientMap; // canvas for the intermediate map
  PImage heatmap; // canvas for the heatmap
  PImage clickmap; // canvas for the clickmap

  float maxValue = 0; // variable storing the current maximum value in the gradientMap
  String test;
  ArrayList targets; 
  //
  PImage img;
  boolean saveBool;
  
  public void setup() {
    size(w, h);
    frameRate(25);
    cp5 = new ControlP5(this);
    cp5.addSlider("abc").setRange(0, 255).setPosition(10,10);
    cp5.addSlider("def").plugTo(parent,"def").setRange(0, 255).setPosition(10,30);
    img = loadImage(spath + java.io.File.separator + "legend4.png");
    
    targets = new ArrayList();

    //targets.add(new Coordinates(95272,22684)); //(2272.7966, 683.9752)
    
    heatmapColors = loadImage(spath + java.io.File.separator +"heatmapColors.png");
    heatmapBrush = loadImage(spath + java.io.File.separator +"heatmapBrushTiny.png");
    clickmapBrush = loadImage(spath + java.io.File.separator +"clickmapBrush.png");
    // create empty canvases:
    clickmap = createImage(img.width, img.height, ARGB);
    gradientMap = new PImage(img.width, img.height);
    heatmap = new PImage(img.width, img.height);
    // load pixel arrays for all relevant images
    gradientMap.loadPixels();
    heatmap.loadPixels();
    heatmapBrush.loadPixels();
    heatmapColors.loadPixels();
  }

  public void draw() {

      image(img,6,1);
      fill(0);

      for (int i=0; i<targets.size(); i++){
 
        //heatmapStuff(x4,y4);
        image(img,6,1);
        tint(255,255,255,125);
        image(heatmap, 6, 1);
        noTint();
        
        
      }
      class Coordinates {
        float x;
        float y;
        Coordinates(float x_in, float y_in){//constructor
          x= x_in;
          y =y_in;
        }
      }
    if(saveBool){
        cf.saveFrame(spath + java.io.File.separator + "heatMap.png");
        saveBool= false;
      }
  }//end draw
  void heatmapStuff(int x_point,int y_point) {
    if (x_point >= 0 && x_point < img.width && y_point >= 0 && y_point < img.height) // we're only concerned about clicks in the upper right image!
    {
      // blit the clickmapBrush onto the (offscreen) clickmap:
      clickmap.blend(clickmapBrush, 0,0,clickmapBrush.width,clickmapBrush.height,x_point-clickmapBrush.width/2,y_point-clickmapBrush.height/2,clickmapBrush.width,clickmapBrush.height,BLEND);
      // blit the clickmapBrush onto the background image in the upper left corner:
      image(clickmapBrush, (x_point-clickmapBrush.width/2)+0, (y_point-clickmapBrush.height/2)+1);
      
      // render the heatmapBrush into the gradientMap:
      drawToGradient(x_point, y_point);
      // update the heatmap from the updated gradientMap:
      updateHeatmap();
      
      // draw the gradientMap in the lower left corner:
      //image(gradientMap, 0, img.height);
      
      // draw the background image in the upper right corner and transparently blend the heatmap on top of it:
      //image(img, img.width,0);
//      tint(255,255,255,100);   **moved to draw (above)
//      image(heatmap, 50, 1);
//      noTint();
      
      // draw the raw heatmap into the bottom right corner and draw the clickmap on top of it:
      //image(heatmap, img.width, img.height);
      //image(clickmap, img.width, img.height);
    }
  }
  void drawToGradient(int x, int y)
  {
    // find the top left corner coordinates on the target image
    int startX = x-heatmapBrush.width/2;
    int startY = y-heatmapBrush.height/2;
  
    for (int py = 0; py < heatmapBrush.height; py++)
    {
      for (int px = 0; px < heatmapBrush.width; px++) 
      {
        // for every pixel in the heatmapBrush:
        
        // find the corresponding coordinates on the gradient map:
        int hmX = startX+px;
        int hmY = startY+py;
        /*
        The next if-clause checks if we're out of bounds and skips to the next pixel if so.
        
        Note that you'd typically optimize by performing clipping outside of the for loops!
        */
        if (hmX < 0 || hmY < 0 || hmX >= gradientMap.width || hmY >= gradientMap.height)
        {
          continue;
        }
        
        // get the color of the heatmapBrush image at the current pixel.
        int col = heatmapBrush.pixels[py*heatmapBrush.width+px]; // The py*heatmapBrush.width+px part would normally also be optimized by just incrementing the index.
        col = col & 0xff; // This eliminates any part of the heatmapBrush outside of the blue color channel (0xff is the same as 0x0000ff)
        
        // find the corresponding pixel image on the gradient map:
        int gmIndex = hmY*gradientMap.width+hmX;
        
        if (gradientMap.pixels[gmIndex] < 0xffffff-col) // sanity check to make sure the gradient map isn't "saturated" at this pixel. This would take some 65535 clicks on the same pixel to happen. :)
        {
          gradientMap.pixels[gmIndex] += col; // additive blending in our 24-bit world: just add one value to the other.
          if (gradientMap.pixels[gmIndex] > maxValue) // We're keeping track of the maximum pixel value on the gradient map, so that the heatmap image can display relative click densities (scroll down to updateHeatmap() for more)
          {
            maxValue = gradientMap.pixels[gmIndex];
            //println(maxValue);
            //println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
          }
          maxValue = 2000.0;
        }
      }
    }
    gradientMap.updatePixels();
  }
  void updateHeatmap()
  {
    // for all pixels in the gradient:
    for (int i=0; i<gradientMap.pixels.length; i++)
    {
      // get the pixel's value. Note that we're not extracting any channels, we're just treating the pixel value as one big integer.
      // cast to float is done to avoid integer division when dividing by the maximum value.
      float gmValue = gradientMap.pixels[i];
      
      // color map the value. gmValue/maxValue normalizes the pixel from 0...1, the rest is just mapping to an index in the heatmapColors data.
      int colIndex = (int) ((gmValue/maxValue)*(heatmapColors.pixels.length-1));
      if (colIndex > (heatmapColors.pixels.length-1)){
        colIndex = (heatmapColors.pixels.length-1);
      }
      int col = heatmapColors.pixels[colIndex];
      
  
      // update the heatmap at the corresponding position
      heatmap.pixels[i] = col;
    }
    // load the updated pixel data into the PImage.
    heatmap.updatePixels();
  }
  
  private ControlFrame() {
  }

  public ControlFrame(Object theParent, int theWidth, int theHeight) {
    parent = theParent;
    w = theWidth;
    h = theHeight;
  }


  public ControlP5 control() {
    return cp5;
  }
  
  
  ControlP5 cp5;

  Object parent;
  
  
}

