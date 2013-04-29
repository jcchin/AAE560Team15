void movingGraph(){
  // Draw lines connecting all points
  float sum = totalBurgs + totalEncounters + totalArrests+.0001;
  for (int i = 0; i < tB.length-1; i++) {
    fill(0);
    stroke(0);
    rect(xoffset+50,offset-60,200,60); //moving percent block
    fill(100);
    stroke(90,0,0);
    strokeWeight(1);
    fill(90,0,0);
    line(i+xoffset,tB[i],i+1+xoffset,tB[i+1]);
    text(nf((totalBurgs/sum)*100.,2,1)+"%",xoffset+80,tB[i]);
    stroke(0,90,0);
    fill(0,90,0);
    line(i+xoffset,tPe[i],i+1+xoffset,tPe[i+1]);
    text(nf((totalEncounters/sum)*100.,2,1)+"%",xoffset+110,tPe[i]);
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
