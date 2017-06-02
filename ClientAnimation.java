import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.io.*;
import javax.imageio.*;
import java.awt.image.*;

public class ClientAnimation extends JPanel implements ActionListener{
  //properties
  static Map currentMap;
  static UserInterface UI; //create a new user interface to thread
  static Thread UI_Thread;
  
  static String [] playerClass = {"NA", "NA", "NA", "NA"};
  int intPlayerNumber;
  int characterSelected = 0;
  int imgX;
  int imgY;
  
  Timer timer;
  Timer timerAtk;
  Timer shortTimer = null;
  int intSAnim = 0; //These replace the boolean variables for slow and fast
  int intFAnim = 2;
  int intAtkAnim = 0;
  int intSlowAnimate = 0;
  int mouseButton = 0; //mouse button
  
  //========================================
  //--Player Sprites--
  static BufferedImage MageImg = null;
  static BufferedImage BuffImg = null;
  static BufferedImage ThiefImg = null;
  static BufferedImage DoctorImg = null;
  BufferedImage EnemySharkImg = null;
  BufferedImage EnemyGhostImg = null;
  int intCharacter = 0;
  
  public void actionPerformed (ActionEvent evt) {
    if (evt.getSource() == timer) {
      
      //for slower animation
      //intSlowAnimate Counts to 2, if its at 1: nothing happens, if its at 2:then the animation switch is triggered.  Then the count is reset back to 0
      intSlowAnimate++;
      if (intSlowAnimate%2 == 0){
        if(intSAnim == 1){
          intSAnim = 0;
          intSlowAnimate =0;
        }else if(intSAnim == 0){
          intSAnim = 1;
        }
      }
      
      //for regular animation
      //intFAnim switches on the timer
      if (intFAnim == 3){
        intFAnim = 2;
      }else if (intFAnim == 2){
        intFAnim = 3;
      }
    }
    
    if(evt.getSource() == shortTimer){
      mouseButton = 0; //reset mouse button after somebody clicks
      shortTimer.stop();
    }


    if (evt.getSource() == timerAtk){
      if(intAtkAnim ==0){
        intAtkAnim=1;
      }else{
        timerAtk.stop();
      }
    }
  }
  
  //methods
  public void paintComponent (Graphics g){
    //EXTRAS!
    //Anti Aliasing
    super.paintComponent(g);
    Graphics2D graphics2D = (Graphics2D) g;
    graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
    graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); 
    
    g.setColor (Color.BLACK);
    g.fillRect (0, 0, 2000, 2000);
    //Setting image coordinates
    imgX = -ClientMain.intPlayerData [intPlayerNumber][0] + 640;
    imgY = -ClientMain.intPlayerData [intPlayerNumber][1] + 360;
    if (imgX > 0) {
      imgX = 0;
    }else if (imgX < -currentMap.MapImg.getWidth() + 1280) {
      imgX = -currentMap.MapImg.getWidth() + 1280;
    }
    if (imgY > 0) {
      imgY = 0;
    }else if (imgY < -currentMap.MapImg.getHeight() + 720) {
      imgY = -currentMap.MapImg.getHeight() + 720;
    }
    //drawing the map background
    g.drawImage (currentMap.MapImg, 0, 0, 1300, 740, -imgX, -imgY, -imgX + 1300, -imgY + 740, null);
    //drawing player 
    
    for (int intCount = 0; intCount < 4; intCount ++) {
      int intRow = 0;
      int intCol = 0;
      int intPlayerX = imgX + ClientMain.intPlayerData [intCount][0] - 45;
      int intPlayerY = imgY + ClientMain.intPlayerData [intCount][1] - 45;
      intRow = (ClientMain.intPlayerData [intCount][2] + 45 - (ClientMain.intPlayerData [intCount][2] + 45)%90)/90;
      
      if (ClientMain.intPlayerData [intCount][3] == 0) {
        intCol = intSAnim;
      }else {
        intCol = intFAnim;
      }

      double timeSinceAtk1 = (System.nanoTime()-ClientMain.longPlayerCooldown[intCount][0])/1e6;
      double timeSinceAtk2 = (System.nanoTime()-ClientMain.longPlayerCooldown[intCount][1])/1e6;
      //shows most attack animation
      if(timeSinceAtk1 < timeSinceAtk2){
        if(timeSinceAtk2 > 0 && timeSinceAtk2 < Integer.parseInt(UI.strDataStats[characterSelected][4]) ){
          intCol = 5;
        }
        if(timeSinceAtk1 > 0 && timeSinceAtk1 < Integer.parseInt(UI.strDataStats[characterSelected][2]) ){
          intCol = 4;
        }
      }else{
        if(timeSinceAtk1 > 0 && timeSinceAtk1 < Integer.parseInt(UI.strDataStats[characterSelected][2]) ){
          intCol = 4;
        }
        if(timeSinceAtk2 > 0 &&timeSinceAtk2 < Integer.parseInt(UI.strDataStats[characterSelected][4]) ){
          intCol = 5;
        }
      }
      
      if (intPlayerX > 0 && intPlayerY > 0) {
        if (playerClass [intCount].equals ("mage")) {
          g.drawImage (MageImg, intPlayerX, intPlayerY, intPlayerX + 90, intPlayerY + 90, 90 * intCol, 90 * intRow, 90 * intCol + 90, 90 * intRow + 90, null);
        }else if (playerClass [intCount].equals ("buff")) {
          g.drawImage (BuffImg, intPlayerX, intPlayerY, intPlayerX + 90, intPlayerY + 90, 90 * intCol, 90 * intRow, 90 * intCol + 90, 90 * intRow + 90, null);
        }else if(playerClass [intCount].equals ("thief")){
          g.drawImage (ThiefImg, intPlayerX, intPlayerY, intPlayerX + 90, intPlayerY + 90, 90 * intCol, 90 * intRow, 90 * intCol + 90, 90 * intRow + 90, null);
        }else if(playerClass [intCount].equals ("doctor")){
          g.drawImage (DoctorImg, intPlayerX, intPlayerY, intPlayerX + 90, intPlayerY + 90, 90 * intCol, 90 * intRow, 90 * intCol + 90, 90 * intRow + 90, null);
        }
      }
    }
    
    if(ClientMain.longEnemyData != null){
      for (int Count = 0; Count < ClientMain.longEnemyData.length; Count ++) {
        //flash if enemy is hit
        Double timeSinceEnemyWasHit = (System.nanoTime()-ClientMain.longEnemyData[Count][3])/1e6;
        int flashSpeed = 70;
        BufferedImage enemyType = null;
        if(ClientMain.longEnemyData [Count][4] == 0){
          enemyType = EnemySharkImg;
        }else if(ClientMain.longEnemyData [Count][4] == 1){
          enemyType = EnemyGhostImg;
        }else{
          enemyType = EnemySharkImg;
        }
        if( timeSinceEnemyWasHit > 0 && timeSinceEnemyWasHit < flashSpeed || timeSinceEnemyWasHit > flashSpeed*3 && timeSinceEnemyWasHit < flashSpeed*4 || timeSinceEnemyWasHit > flashSpeed*6 && timeSinceEnemyWasHit < flashSpeed * 6 || timeSinceEnemyWasHit > flashSpeed*8 && timeSinceEnemyWasHit < flashSpeed*9){
          //invisible (flashing enemy hit animation)
        }else{
          //draw shark
          if(ClientMain.longEnemyData[Count][2] >= 90 && ClientMain.longEnemyData[Count][2] <= 270){
            //facing left
            if(intSAnim == 1){
              g.drawImage (enemyType, imgX+(int)ClientMain.longEnemyData[Count][0]-45,  imgY+(int)(ClientMain.longEnemyData [Count][1])-45, imgX+(int)ClientMain.longEnemyData[Count][0]+45, imgY+(int)(ClientMain.longEnemyData [Count][1])+45, 0, 0, 90, 90, null);
            }else{
              g.drawImage (enemyType, imgX+(int)ClientMain.longEnemyData[Count][0]-45,  imgY+(int)(ClientMain.longEnemyData [Count][1])-45, imgX+(int)ClientMain.longEnemyData[Count][0]+45, imgY+(int)(ClientMain.longEnemyData [Count][1])+45, 90, 0, 180, 90, null);
            }
          }else{
            //facing right
            if(intSAnim == 1){
              g.drawImage (enemyType, imgX+(int)ClientMain.longEnemyData[Count][0]-45,  imgY+(int)(ClientMain.longEnemyData [Count][1])-45, imgX+(int)ClientMain.longEnemyData[Count][0]+45, imgY+(int)(ClientMain.longEnemyData [Count][1])+45, 0, 90, 90, 180, null);
            }else{
              g.drawImage (enemyType, imgX+(int)ClientMain.longEnemyData[Count][0]-45,  imgY+(int)(ClientMain.longEnemyData [Count][1])-45, imgX+(int)ClientMain.longEnemyData[Count][0]+45, imgY+(int)(ClientMain.longEnemyData [Count][1])+45, 90, 90, 180, 180, null);
            }
          }
          //g.fillOval (imgX + (int)ClientMain.longEnemyData [Count][0] - 30, imgY + (int)(ClientMain.longEnemyData [Count][1]) - 30, 60, 60);
        }
      }
    }
    
    //update mininmap with player choordinates
    currentMap.updateMinimap(ClientMain.intPlayerData, playerClass, intPlayerNumber);
    
    //update UserInterface class
    if(UI != null){ 
      UI.mouseButton = this.mouseButton;
      //draw User Interface
      g.drawImage(UI.UIRender, 0, 0, null);
    }
  }
  
  
  
  
//constructor
  public ClientAnimation (String currentMapFile) throws IOException{
    super(true);
    timer = new Timer(450, this);
    timer.start();
    timerAtk = new Timer(150, this);
    
    //Initialize user interface
    UI = new UserInterface();
    UI_Thread = new Thread(UI);
    
    shortTimer = new Timer(50, this);
    currentMap = new Map (currentMapFile + ".csv");
    try{
      currentMap.renderMap();
      currentMap.renderMinimap();
      currentMap.renderBoundMap();
      MageImg = ImageIO.read(new File("Mageimg.png"));
      BuffImg = ImageIO.read(new File("BuffImg.png"));
      ThiefImg = ImageIO.read(new File("ThiefImg.png"));
      DoctorImg = ImageIO.read(new File("DoctorImg.png"));
      EnemySharkImg = ImageIO.read(new File("EnemySharkImg.png"));
      EnemyGhostImg = ImageIO.read(new File("EnemyGhostImg.png"));
    }catch(Exception e){
      System.out.println("Render map error: " + e);
    }
    //start rendering the UI
    UI_Thread.start();
  }
}