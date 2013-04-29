void visualize2(){
  println(line2);
  String[] road_pieces = split(line2, ':');
  
  float roadX = -100000*(float(road_pieces[1])+110.00);
  float roadY = 100000*(float(road_pieces[2])-32.00);

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
