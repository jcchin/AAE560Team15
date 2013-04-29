void update(int x, int y) {
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

void mousePressed() {
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

boolean overRect(int x, int y, int width, int height)  {
  if (mouseX >= x && mouseX <= x+width && 
      mouseY >= y && mouseY <= y+height) {
    return true;
  } else {
    return false;
  }
}
