import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.awt.Frame; 
import java.awt.BorderLayout; 
import controlP5.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class burgleAnalyze extends PApplet {

BufferedReader reader;
BufferedReader road_reader;
PrintWriter output;
PImage img;
ArrayList targets;

int rectX, rectY;      // Position of square button
int rectColor;
int rectHighlight;
int currentColor;
boolean rectOver = false;
boolean rectOver2 = false;
int rectSize = 20;     // Diameter of rect
String path = "C:\\Users\\Owner\\aae560\\BurglaryABM\\results2.txt"; //Jeff Laptop
String path2 = "C:\\AAE560\\aae560\\BurglaryABM\\results2.txt"; //Dan and Chris Path
String path3 = "C:\\Users\\jcchin\\aae560\\BurglaryABM\\results2.txt"; //Jeff Work
String line; String line2;
String spath; String truPath; String truPath2; int index;
boolean once = true;

int totalBurgs;
int totalEncounters;
int totalArrests;
int totalAlarms;
int dist0_05=0;  int dist05_1=0;  int dist1_15=0; int dist15_2=0; int dist2_25=0; int dist25_3=0; int dist3_35=0; int dist35plus=0;

//heat map stuff
PImage backgroundImage; // background image
PImage heatmapBrush; // radial gradient used as a brush. Only the blue channel is used.
PImage heatmapColors; // single line bmp containing the color gradient for the finished heatmap, from cold to hot
PImage clickmapBrush; // bmp of the little marks used in the clickmap

PImage gradientMap; // canvas for the intermediate map
PImage heatmap; // canvas for the heatmap
PImage clickmap; // canvas for the clickmap

float maxValue = 0; // variable storing the current maximum value in the gradientMap
//window2stuff




private ControlP5 cp5;

ControlFrame cf;

int def;

//moving graph stuff
float[] tB;
float[] tPe;
float[] tA;
int offset = 280;
int xoffset = 175;
//
public void setup() {
  frameRate(240);
  size(740,760);
  background(0);
  textSize(11);
  targets = new ArrayList();
  spath = sketchPath("");
  index = spath.indexOf("burgleAn");
  truPath = spath.substring(0,index-1)+ java.io.File.separator + "BurglaryABM" + java.io.File.separator + "resultsJeff.txt"; //"resultsJeff.txt";"RoadTravel.txt";"RealCrimeData.txt";
  //truPath = spath.substring(0,index-1)+ java.io.File.separator + "BurglaryABM" + java.io.File.separator + "CrimeValidationLatLong.txt";
  //truPath = spath.substring(0,index-1)+ java.io.File.separator + "BurglaryABM" + java.io.File.separator + "CrimeValidationLatLong_Extended.txt";
  println(truPath);
  
  truPath2 = spath.substring(0,index-1)+ java.io.File.separator + "BurglaryABM" + java.io.File.separator + "RoadTravel.txt"; //"CrimeValidationLatLong.txt";
  //point to file
  try {
    reader = createReader(truPath); //
    //line = reader.readLine();
    //visualize();
    road_reader = createReader(truPath2);
  } catch (Exception e) {
    println(e + ". used " + path + " instead");
    try{
    reader = createReader(path);
    } catch (Exception e2) {
      println(e2 + ". used Jeff's other path instead"); //work computer
      reader = createReader(path3);
    }
  }
  
  fill(255);
  text("<-Click this before every new run.", 90,15);
  text("<-Click this to save heatmap \"heatMap.png\"", 390,15);
  rectColor = color(0); //normal button color
  rectHighlight = color(150); //highlighted button color
  rect(0, 30, rectSize*20, rectSize);
  rect(0, 60, rectSize*20, rectSize);
  rect(0, 90, rectSize*20, rectSize);
  text("money",410,45);
  text("social",410,75);
  text("opportunity",410,105);
  
  //window2stuff
  cp5 = new ControlP5(this);
  
  // by calling function addControlFrame() a
  // new frame is created and an instance of class
  // ControlFrame is instanziated.
  cf = addControlFrame("extra", 1250,950);
  
  
  int totalBurgs=0;
  int totalEncounters = 0;
  int totalArrests=0;
  int totalAlarms=0;
  
  //moving graph stuff
  tB = new float[50]; tPe = new float[50]; tA = new float[50];
  for (int i = 0; i < tB.length; i++) {
    tB[i]=offset- totalBurgs; 
    tPe[i]=offset-totalEncounters;
    tA[i]=offset-totalArrests;
  }
}
 
public void draw() {
  update(mouseX, mouseY);
  stroke(255); // button outline
  if (rectOver){
    fill(rectHighlight);
  }else{
    fill(rectColor);
  }
  rect(0, 0, rectSize*4, rectSize); //draw button, [x,y corner], width, height
  if (rectOver2){
    fill(rectHighlight);
  }else{
    fill(rectColor);
  }
  rect(300, 0, rectSize*4, rectSize); //draw button, [x,y corner], width, height
  fill(255);
  text("Clear File", 2,15);
  text("Save Map", 316,15);
  fill(0,255,0);

  //-----------------Data Stream--------------------
  try {
    line = reader.readLine();
  } catch (IOException e) {
    e.printStackTrace();
    line = null;
    
  }
  if (line == null) {
    // Stop reading because of an error or file is empty
    //noLoop(); 
  } else {
    boolean once = true;
    visualize(); 
  }
  
  try {
    line2 = road_reader.readLine();
  } catch (IOException e2) {
    e2.printStackTrace();
    line2 = null;
    
  }
  if (line2 == null) {
    // Stop reading because of an error or file is empty
    //noLoop(); 
  } else {
    //visualize2(); 
  }
  
  fill(0);
  stroke(0);
  rect(0,230,300,80);
  rect(xoffset,offset-55,200,60); //moving graph block
  rect(100,220,650,400); //frequency block
  fill(255);
  text("Total Burglaries: " + str(totalBurgs),5,250);
  text("Total Police Encounters: " + str(totalEncounters),5,265);
  //text("Total Arrests: " + str(totalArrests),5, 280);
  text("Total Arrests: " + str(totalArrests) + " - " + str(totalAlarms),5, 280);
  
  text("Distance Traveled To Burgle:(normalized)  0-0.5      0.5-1       1-1.5       1.5-2       2-2.5       2.5-3      3-3.5      3.5+ (miles)",100, 450);
  float histSum = dist0_05+dist05_1+dist1_15+dist15_2+dist2_25+dist25_3+dist3_35+dist35plus+.01f;
  rect(330,440-(100*dist0_05/histSum),20,1+(100*dist0_05/histSum));
  rect(380,440-(100*dist05_1/histSum),20,1+(100*dist05_1/histSum));
  rect(430,440-(100*dist1_15/histSum),20,1+(100*dist1_15/histSum));
  rect(480,440-(100*dist15_2/histSum),20,1+(100*dist15_2/histSum));
  rect(530,440-(100*dist2_25/histSum),20,1+(100*dist2_25/histSum));
  rect(580,440-(100*dist25_3/histSum),20,1+(100*dist25_3/histSum));
  rect(630,440-(100*dist3_35/histSum),20,1+(100*dist3_35/histSum));
  rect(680,440-(100*dist35plus/histSum),20,1+(100*dist35plus/histSum));
  
  movingGraph();
  
} 
public void update(int x, int y) {
 if (overRect(0,0,80,20)) {
    //fill(rectHighlight);
    rectOver = true;
    rectOver2 = false;
  } else if(overRect(300,0,80,30)) {
    //fill(rectColor);
    rectOver = false;
    rectOver2 = true;
  } else { 
    //fill(rectColor);
    rectOver = false;
    rectOver2 = false;
  }
}

public void mousePressed() {
  if (rectOver) {
    output = createWriter(truPath);
    for (int i = 0; i < 18; i = i+1) {
      output.println("need_18_line_header"); //needs at least one line
    }
    output.flush(); // Writes the remaining data to the file
    output.close(); // Finishes the file
    setup();
  }else if(rectOver2){
   cf.saveBool = true; 
  }
}

public boolean overRect(int x, int y, int width, int height)  {
  if (mouseX >= x && mouseX <= x+width && 
      mouseY >= y && mouseY <= y+height) {
    return true;
  } else {
    return false;
  }
}
//Top-Left    (-110.936219, 32.228753) ---.--600,--.--845        600,810----------------006,810
//Top-Right   (-110.930527, 32.228244) ---.--035,--.--845               |               |
//Bottom-Left (-110.935839, 32.224698) ---.--600,--.--475               |               |
//Bottom-Right(-110.930211, 32.224812) ---.--035,--.--475        600,445----------------006,445  
//                                                                 |               |
//                                                                 0,365----------------594,365 
//TL -110.974531, 32.240828
//TR -110.918655, 32.240828
//BL -110.974531, 32.197986
//BR -110.918655, 32.197986

class Coordinates {
  float x;
  float y;
  Coordinates(float x_in, float y_in){//constructor
    x= x_in;
    y =y_in;
  }
}
public void movingGraph(){
  // Draw lines connecting all points
  float sum = totalBurgs + totalEncounters + totalArrests+.0001f;
  for (int i = 0; i < tB.length-1; i++) {
    fill(0);
    stroke(0);
    rect(xoffset+50,offset-60,200,60); //moving percent block
    fill(100);
    stroke(90,0,0);
    strokeWeight(1);
    fill(90,0,0);
    line(i+xoffset,tB[i],i+1+xoffset,tB[i+1]);
    text(nf((totalBurgs/sum)*100.f,2,1)+"%",xoffset+80,tB[i]);
    stroke(0,90,0);
    fill(0,90,0);
    line(i+xoffset,tPe[i],i+1+xoffset,tPe[i+1]);
    text(nf((totalEncounters/sum)*100.f,2,1)+"%",xoffset+110,tPe[i]);
    stroke(20,20,90);
    fill(40,40,130);
    line(i+xoffset,tA[i],i+1+xoffset,tA[i+1]);
    text(nf((totalArrests/sum)*100,2,1)+"%",xoffset+50,tA[i]);
    fill(100);
  }
  
  // Slide everything down in the array
  for (int i = 0; i < tB.length-1; i++) {
    tB[i] = tB[i+1]; 
    tPe[i] = tPe[i+1];
    tA[i] = tA[i+1];
  }
  // Add a new random value
  
  tB[tB.length-1] = offset-(totalBurgs/sum)*50;
  tPe[tPe.length-1] = offset-(totalEncounters/sum)*50;
  tA[tA.length-1] = offset-(totalArrests/sum)*50;

}
public void visualize(){
  println(line);
  //println(line);
  String[] pieces = split(line, ':');
  if (PApplet.parseInt(pieces[0])==-1){
    setup();
  }
  if (pieces.length >= 2){ //skip bogus lines
  if (pieces[0].length() < 3 && pieces.length == 7){
    float ID = PApplet.parseFloat(pieces[0]); //agent ID
    float money = PApplet.parseFloat(pieces[1]); //agent money
    float social = PApplet.parseFloat(pieces[2]);
    float opp = PApplet.parseFloat(pieces[3]);
    float moneyP = PApplet.parseFloat(pieces[4]);
    float socialP = PApplet.parseFloat(pieces[5]);
    String status = pieces[6];
    
    fill(0);
    stroke(0);
    rect(50,180,300,40);//box to cover text (otherwise it will write on top)
    fill(255);
    textSize(15);
    String[] pieces2 = split(status, ',');
    if (pieces2.length ==2){
      String status2 = (pieces2[0]);//agent status
      String timer = (pieces2[1]);//agent status
      text(status2, 120, 200);//display text
      text("for " + timer + " more ticks", 120, 215);
    } else if(status.equals("Agent Caught!")||status.equals("Agent Caught!alarm")){
      totalArrests = totalArrests + 1;
      if(status.equals("Agent Caught!alarm"))totalAlarms = totalAlarms +1;
      text(status, 120, 200);
    } else if(status.equals("Police Encountered!")){
      totalEncounters = totalEncounters + 1;
      text(status, 120, 200);
    } else{
      text(status, 120, 200);//display text
    }
    textSize(11);
    fill(200);
    stroke(255);
    //base bars
    rect(0, 30, rectSize*20, rectSize); 
    rect(0, 60, rectSize*20, rectSize);
    rect(0, 90, rectSize*20, rectSize);
   
    fill(190,00,190);
    rect(0, 30, rectSize*20*money, rectSize); //draw button, [x,y corner], width, height
    rect(0, 60, rectSize*20*social, rectSize); //draw button, [x,y corner], width, height
    rect(0, 90, rectSize*20*opp-5, rectSize); //draw button, [x,y corner], width, height
    //Probability Bar
    fill(0,245,0);
    stroke(0);
    rect(0, 120, rectSize*20, rectSize);//default green bar
    fill(0);
    stroke(0);
    rect(0,142,360,15); //clear text box
    fill(255,0,0);
    rect(0, 120, rectSize*20*moneyP, rectSize); //burglary
    text("burglary %", 50, 150);
    fill(0,0,255);
    rect(rectSize*20*moneyP, 120, (1-moneyP)*rectSize*20*socialP, rectSize);
    text("social %", 180, 150);
    fill(0,255,0);
    text("work %", 310, 150);
  }
  if (pieces[0].equals("MAP")){ //map point
  //MAP:(-110.9340299216221, 32.2255811279421, NaN)
    println("coords: " + pieces[1]);
    String[] coords0 = split(pieces[1], '(');
    String[] coords = split(coords0[1], ',');
    float newX = -100000*(PApplet.parseFloat(coords[0])+110.00f);
    float newY =100000*(PApplet.parseFloat(coords[1])-32.00f);
    float newDist =100000*PApplet.parseFloat(coords[2]);
    //newX = newX + random(-150,150);
    //newY = newY + random(-150,150); //add randomness
    println(newX);
    println(newY);
    println(newDist);

    //targets.add(new Coordinates(newX,newY));//add to victim list
    cf.targets.add(new Coordinates(newX,newY));//add to victim list
    //TL -110.974531, 32.240828
      //TR -110.918655, 32.240828
      //BL -110.974531, 32.197986
      //BR -110.918655, 32.197986
    int cornx=8; int corny=1; //image left top corner
    int imageHeight = 950; int imageWidth = 1234; //png pixel dimension
    int mapCornx = 98213; int mapCornY =24082; //coordinates defining top left corner
    int mapHeight = 4554; int mapWidth= 6593; //map coordinate total size
    float x1 = (mapCornx-newX);
    float y1 = (mapCornY-newY);
    float x2 = lerp(0,imageWidth,(x1/mapWidth)); //495 image width
    float y2 = lerp(0,imageHeight,(y1/mapHeight)); //361 image height
    float x3 = (x2+cornx);
    float y3 = (y2 + corny);
    int x4 = round(x3);
    int y4 = round(y3);
    cf.heatmapStuff(x4,y4);
    
    totalBurgs = totalBurgs + 1;
    
    
    //32.227741,-110.968716
    //32.227814,-110.943933
    //2478.3 = 1.4 miles
    //1770.2 = 1 mile
    newDist = newDist/1770.2f;
    println("Miles traveled! : " + str(newDist));
    
    if (newDist >= 0.f && newDist < 0.5f){
      dist0_05 = dist05_1+1;
    }
    if (newDist >= 0.5f && newDist < 1.0f){
      dist05_1 = dist05_1+1;
    }
    if (newDist >= 1 && newDist < 1.5f){
      dist1_15 = dist1_15+1;
    }
    if (newDist >= 1.5f && newDist < 2.0f){
      dist15_2 = dist15_2+1;
    }
    if (newDist >= 2.0f && newDist < 2.5f){
      dist2_25 = dist2_25+1;
    }
    if (newDist >= 2.5f && newDist < 3.0f){
      dist25_3 = dist25_3+1;
    }
    if (newDist >= 3.0f && newDist < 3.5f){
      dist3_35 = dist3_35+1;
    }
    if (newDist >= 3.5f){
      dist35plus = dist35plus+1;
    }
  } 
  }
}



public void visualize2(){
  println(line2);
  String[] road_pieces = split(line2, ':');
  
  float roadX = -100000*(PApplet.parseFloat(road_pieces[1])+110.00f);
  float roadY = 100000*(PApplet.parseFloat(road_pieces[2])-32.00f);

  println(roadX);
  println(roadY);
  
  cf.targets.add(new Coordinates(roadX,roadY));//add to victim list
    //TL -110.974531, 32.240828
    //TR -110.918655, 32.240828
    //BL -110.974531, 32.197986
    //BR -110.918655, 32.197986
  int cornx=8; int corny=1; //image left top corner
  int imageHeight = 950; int imageWidth = 1234; //png pixel dimension
  int mapCornx = 98213; int mapCornY =24082; //coordinates defining top left corner
  int mapHeight = 4554; int mapWidth= 6593; //map coordinate total size
  float x1 = (mapCornx-roadX);
  float y1 = (mapCornY-roadY);
  float x2 = lerp(0,imageWidth,(x1/mapWidth)); //495 image width
  float y2 = lerp(0,imageHeight,(y1/mapHeight)); //361 image height
  float x3 = (x2+cornx);
  float y3 = (y2 + corny);
  int x4 = round(x3);
  int y4 = round(y3);
  cf.heatmapStuff(x4,y4);
  
}

public ControlFrame addControlFrame(String theName, int theWidth, int theHeight) {
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
  public void heatmapStuff(int x_point,int y_point) {
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
  public void drawToGradient(int x, int y)
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
          maxValue = 2000.0f;
        }
      }
    }
    gradientMap.updatePixels();
  }
  public void updateHeatmap()
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

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "burgleAnalyze" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
