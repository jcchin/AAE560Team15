BufferedReader reader;
BufferedReader road_reader;
PrintWriter output;
PImage img;
ArrayList targets;

int rectX, rectY;      // Position of square button
color rectColor;
color rectHighlight;
color currentColor;
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
import java.awt.Frame;
import java.awt.BorderLayout;
import controlP5.*;

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
void setup() {
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
 
void draw() {
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
  float histSum = dist0_05+dist05_1+dist1_15+dist15_2+dist2_25+dist25_3+dist3_35+dist35plus+.01;
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
