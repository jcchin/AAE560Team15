void visualize(){
  println(line);
  //println(line);
  String[] pieces = split(line, ':');
  if (int(pieces[0])==-1){
    setup();
  }
  if (pieces.length >= 2){ //skip bogus lines
  if (pieces[0].length() < 3 && pieces.length == 7){
    float ID = float(pieces[0]); //agent ID
    float money = float(pieces[1]); //agent money
    float social = float(pieces[2]);
    float opp = float(pieces[3]);
    float moneyP = float(pieces[4]);
    float socialP = float(pieces[5]);
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
    float newX = -100000*(float(coords[0])+110.00);
    float newY =100000*(float(coords[1])-32.00);
    float newDist =100000*float(coords[2]);
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
    newDist = newDist/1770.2;
    println("Miles traveled! : " + str(newDist));
    
    if (newDist >= 0. && newDist < 0.5){
      dist0_05 = dist05_1+1;
    }
    if (newDist >= 0.5 && newDist < 1.0){
      dist05_1 = dist05_1+1;
    }
    if (newDist >= 1 && newDist < 1.5){
      dist1_15 = dist1_15+1;
    }
    if (newDist >= 1.5 && newDist < 2.0){
      dist15_2 = dist15_2+1;
    }
    if (newDist >= 2.0 && newDist < 2.5){
      dist2_25 = dist2_25+1;
    }
    if (newDist >= 2.5 && newDist < 3.0){
      dist25_3 = dist25_3+1;
    }
    if (newDist >= 3.0 && newDist < 3.5){
      dist3_35 = dist3_35+1;
    }
    if (newDist >= 3.5){
      dist35plus = dist35plus+1;
    }
  } 
  }
}



