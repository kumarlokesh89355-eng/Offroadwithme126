package com.offroadwithme.game;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.view.*;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.content.Context;
import android.content.SharedPreferences;
import java.util.Random;
import java.util.Calendar;

public class MainActivity extends Activity {
  @Override public void onCreate(Bundle b){ super.onCreate(b); getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN); getWindow().setNavigationBarColor(Color.BLACK); setContentView(new OffroadView(this)); }
  @Override public void onBackPressed(){ OffroadView v=(OffroadView)((ViewGroup)findViewById(android.R.id.content)).getChildAt(0); if(v!=null && v.garageScreen){v.garageScreen=false;v.levelScreen=true;v.invalidate();} else if(v!=null && v.levelScreen){v.levelScreen=false;v.invalidate();} else if(v!=null){v.paused=true;v.invalidate();} else super.onBackPressed(); }
  @Override protected void onPause(){ super.onPause(); View root=((ViewGroup)findViewById(android.R.id.content)).getChildAt(0); if(root instanceof OffroadView){ OffroadView v=(OffroadView)root; v.persistProgress(); v.paused=true; v.stopControls(); v.invalidate(); } }
  @Override protected void onResume(){ super.onResume(); View root=((ViewGroup)findViewById(android.R.id.content)).getChildAt(0); if(root instanceof OffroadView){ ((OffroadView)root).last=System.nanoTime(); } }
  @Override protected void onSaveInstanceState(Bundle out){ View root=((ViewGroup)findViewById(android.R.id.content)).getChildAt(0); if(root instanceof OffroadView){ ((OffroadView)root).persistProgress(); } super.onSaveInstanceState(out); }
}

class OffroadView extends View {
  Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); Paint stroke=new Paint(Paint.ANTI_ALIAS_FLAG); Random rnd=new Random(7);
  float carX=0, speed=0, distance=0, fuel=100, damage=0, steer=0, roadPhase=0, coins=0, checkpointPulse=0, suspension=0, cameraShake=0, nextCoinCheckpoint=250, introTimer=1.8f, terrainBump=0, rutPhase=0, tractionFeedback=0, steeringVisual=0, bestDistance=0, milestoneTimer=0, savedBestCheckpoint=0, trailTokens=0, runTokens=0, combo=0, comboTimer=0, missionReward=0, servicePulse=0, sectionPulse=0, pickupPulse=0, repairPulse=0; boolean lowFuelWarned=false, edgeWarned=false, finishWarned=false, checkpointWarned=false, serviceWarned=false; boolean[] fuelPickupTaken=new boolean[]{false,false}; boolean[] repairPickupTaken=new boolean[]{false,false}; int lastObstacleHit=-1; boolean paused=false, levelScreen=false, garageScreen=false, nitro=false, throttle=false, brake=false, finished=false, failed=false;
  int level=1, unlocked=1, engine=0, tires=0, selectedVehicle=0; int missionType=0, playerXp=0, winStreak=0; float totalDistance=0; long last; float scale=1; int W,H; boolean left,right;
  SharedPreferences save;
  Vibrator vibrator;
  String[] names={"Village Trail","Bumpy Countryside","Dust Road","Rain Track","Fog Valley","Rock Pass","Forest Mud","Mountain Storm","Night Ridge","Summit Run","Canyon Run","Riverbed","Pine Pass","Red Rock","Frozen Trail","Coastal Cliff","Jungle Route","Desert Dunes","Legend Peak","Moonlight Summit"};
  int[] skies={0xff8fbfd9,0xff9eb9c7,0xffd7b27b,0xff6f8795,0xff71828b,0xff8794a0,0xff6f8a78,0xff46576b,0xff202b45,0xffd9855d,0xffb26b4f,0xff8da5ad,0xff72869a,0xffc16b4e,0xff9ba9b8,0xff6f9bb0,0xff5f8c68,0xffd59b62,0xff31384f,0xff182238};
  int[] grounds={0xff6f9b55,0xff728c4e,0xff8d764d,0xff526c55,0xff53645e,0xff5e675b,0xff455943,0xff3f5145,0xff29382f,0xff665643,0xff7b5742,0xff68715c,0xff50624c,0xff8d6048,0xff78818a,0xff5e8056,0xff416342,0xffa77b4e,0xff3d443f,0xff252d34};
  OffroadView(Context c){super(c); setLayerType(View.LAYER_TYPE_HARDWARE,null); stroke.setStyle(Paint.Style.STROKE); save=c.getSharedPreferences("offroad_save",Context.MODE_PRIVATE); vibrator=(Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE); unlocked=Math.max(1,Math.min(20,save.getInt("unlocked",1))); coins=save.getFloat("coins",0); trailTokens=save.getInt("trailTokens",0); playerXp=save.getInt("playerXp",0); winStreak=save.getInt("winStreak",0); totalDistance=save.getFloat("totalDistance",0); bestDistance=save.getFloat("best_"+level,0); engine=Math.max(0,Math.min(3,save.getInt("engine",0))); tires=Math.max(0,Math.min(3,save.getInt("tires",0))); selectedVehicle=Math.max(0,Math.min(2,save.getInt("selectedVehicle",0))); if(selectedVehicle==1 && playerXp<500) selectedVehicle=0; if(selectedVehicle==2 && winStreak<3) selectedVehicle=0; save.edit().putInt("saveSchema",125).putInt("selectedVehicle",selectedVehicle).apply(); last=System.nanoTime(); setFocusable(true);}
  void persistProgress(){ if(save==null)return; save.edit().putInt("unlocked",Math.max(1,Math.min(20,unlocked))).putFloat("coins",Math.max(0,coins)).putInt("engine",Math.max(0,Math.min(3,engine))).putInt("tires",Math.max(0,Math.min(3,tires))).putInt("selectedVehicle",Math.max(0,Math.min(2,selectedVehicle))).putInt("saveSchema",125).putInt("trailTokens",Math.max(0,trailTokens)).putInt("playerXp",Math.max(0,playerXp)).putInt("winStreak",Math.max(0,winStreak)).putFloat("totalDistance",Math.max(0,totalDistance)).putFloat("best_"+level,Math.max(bestDistance,distance)).apply(); }
  void txt(Canvas c,String s,float x,float y,float size,int color,Paint.Align a){p.setStyle(Paint.Style.FILL);p.setColor(color);p.setTextSize(size);p.setTypeface(Typeface.create("sans",Typeface.BOLD));p.setTextAlign(a);c.drawText(s,x,y,p);}
  void round(Canvas c,float l,float t,float r,float b,float rad,int color){p.setStyle(Paint.Style.FILL);p.setColor(color);c.drawRoundRect(l,t,r,b,rad,rad,p);}
  @Override protected void onSizeChanged(int w,int h,int ow,int oh){W=w;H=h;scale=Math.min(W/800f,H/480f);}
  int todayKey(){ Calendar d=Calendar.getInstance(); return d.get(Calendar.YEAR)*1000+d.get(Calendar.DAY_OF_YEAR); }
  int missionGoal(){ return missionType==0?3:missionType==1?500:1; }
  String missionText(){ if(missionType==0) return "MISSION: COLLECT 3 TRAIL TOKENS"; if(missionType==1) return "MISSION: DRIVE 500 m UNDER 25% DAMAGE"; return "MISSION: FINISH WITH FUEL ABOVE 35%"; }
  boolean missionDone(){ if(missionType==0) return runTokens>=3; if(missionType==1) return distance>=500 && damage<25; return distance>=1000 && fuel>35; }
  void claimDaily(){ int key=save.getInt("dailyKey",0); if(key!=todayKey()){ coins+=25; save.edit().putInt("dailyKey",todayKey()).putFloat("coins",coins).apply(); milestoneTimer=2.2f; haptic(70); } }
  @Override protected void onDraw(Canvas c){super.onDraw(c); if(levelScreen){drawLevels(c);return;} if(garageScreen){drawGarage(c);return;} drawGame(c); long now=System.nanoTime(); float dt=Math.min(.04f,(now-last)/1e9f); last=now; if(!paused && !failed){update(dt);} postInvalidateDelayed(16);}
  void update(float dt){
    if(left)steer=-1; else if(right)steer=1; else steer*=.72f;
    float max=baseSpeed(); if(selectedVehicle==1) max*=1.08f; if(selectedVehicle==2) max*=0.92f;
    // Terrain/weather changes handling without making the early levels frustrating.
    if(level==4 || level==8) max*=.90f;       // rain / storm
    if(level==5) max*=.94f;                  // fog
    if(level==7) max*=.86f;                  // mud
    if(level==15) max*=.88f;                 // snow
    max*=Math.max(.55f,1f-damage*.0025f);    // damaged vehicle loses power
    float traction=1f-Math.min(.28f,Math.abs(steer)*0.10f);
    if(level==7||level==15) traction*=.88f; if(level==1 && distance>400 && distance<520) traction*=.91f; if(level==1 && distance>610 && distance<700) traction*=.95f; if(selectedVehicle==2) traction*=1.10f; if(selectedVehicle==1) traction*=.94f;       // mud / snow needs gentler steering
    if(level==4||level==8) traction*=.94f; if(level==1 && distance>400 && distance<520) max*=.93f; if(level==1 && distance>610 && distance<700) max*=.88f;       // wet surface
    if(throttle && fuel>0.5f && damage<100){ max*= (1f+engine*.08f); max*=nitro?1.65f:1f; speed += (max-speed)*dt*(2.8f*traction); }
    else if(brake){ speed += (-Math.min(18f,speed))*dt*5.0f; }
    else { speed -= speed*dt*1.35f; }
    if(speed<0) speed=0;
    distance+=speed*dt*2.4f; totalDistance+=speed*dt*2.4f; bestDistance=Math.max(bestDistance,distance); if(bestDistance>=savedBestCheckpoint+10f){save.edit().putFloat("best_"+level,bestDistance).apply();savedBestCheckpoint=bestDistance;} roadPhase+=speed*dt*.9f; checkpointPulse+=dt; servicePulse=Math.max(0,servicePulse-dt); sectionPulse=Math.max(0,sectionPulse-dt); pickupPulse=Math.max(0,pickupPulse-dt); milestoneTimer=Math.max(0,milestoneTimer-dt); introTimer=Math.max(0,introTimer-dt); comboTimer=Math.max(0,comboTimer-dt); if(comboTimer<=0) combo=0; if(speed>5 && distance>=nextCoinCheckpoint){
      checkpointWarned=false; coins += 2; trailTokens++; runTokens++; combo++; comboTimer=5f; checkpointPulse=0; milestoneTimer=1.6f; haptic(45);
      // Trail service zones keep long runs strategic: a clean checkpoint gives a small repair/fuel top-up.
      if(nextCoinCheckpoint==500f || nextCoinCheckpoint==750f){ float oldDamage=damage, oldFuel=fuel; damage=Math.max(0,damage-6f); fuel=Math.min(100,fuel+4f); servicePulse=2.4f; sectionPulse=2.4f; if(oldDamage-damage>0.5f || fuel-oldFuel>0.5f) haptic(80); }
      nextCoinCheckpoint += 250; save.edit().putFloat("coins",coins).apply(); }
    // Level 1 recovery pickups: optional fuel cans reward route awareness without making the level easy.
    if(level==1){ int[] pd={320,680}; int[] px={72,-76}; for(int i=0;i<2;i++){ if(!fuelPickupTaken[i] && distance>=pd[i] && distance<pd[i]+18 && Math.abs(carX-px[i])<58){ fuel=Math.min(100,fuel+18); fuelPickupTaken[i]=true; pickupPulse=2.0f; coins+=3; trailTokens++; haptic(70); save.edit().putFloat("coins",coins).putInt("trailTokens",trailTokens).apply(); } } }
    // Level 1 repair kits: a second recovery choice rewards controlled driving after the mud/bridge hazards.
    if(level==1){ int[] rd={470,820}; int[] rx={-82,78}; for(int i=0;i<2;i++){ if(!repairPickupTaken[i] && distance>=rd[i] && distance<rd[i]+18 && Math.abs(carX-rx[i])<58){ damage=Math.max(0,damage-10); repairPickupTaken[i]=true; repairPulse=2.0f; coins+=4; trailTokens++; haptic(85); save.edit().putFloat("coins",coins).putInt("trailTokens",trailTokens).apply(); } } }
    float terrainZone=(float)((distance%500f)/500f);
    float zoneGrip=1f;
    if(level==1 && (terrainZone>.24f&&terrainZone<.38f || terrainZone>.68f&&terrainZone<.82f)) zoneGrip=.90f;
    float steerPower=(35f+speed*2.8f)*(nitro?1.08f:1f)*zoneGrip;
    carX += steer*dt*steerPower;
    carX*=.992f;
    terrainBump += speed*dt*(0.12f + (level==6||level==11||level==14 ? 0.10f : 0f));
    rutPhase += speed*dt*.018f;
    tractionFeedback += (zoneGrip<1f ? dt*speed*.03f : -dt*1.5f); tractionFeedback=Math.max(0,Math.min(1.5f,tractionFeedback));
    steeringVisual += (steer-steeringVisual)*Math.min(1f,dt*7f);
    suspension += (speed*.018f + Math.abs(steer)*.55f) * dt * 8f;
    float bumpShake=(float)Math.abs(Math.sin(terrainBump*2.7f))*Math.min(1.8f,speed*.035f);
    cameraShake = Math.min(3.8f, Math.abs(steer)*speed*.012f + bumpShake);
    if(throttle && speed>2) fuel-=dt*(nitro?1.2f:.18f)*(selectedVehicle==1?1.12f:selectedVehicle==2?.90f:1f); else fuel-=dt*.025f;
    if(fuel<0)fuel=0;
    if(fuel<=20f && fuel>0f && !lowFuelWarned){ lowFuelWarned=true; haptic(90); }
    if(Math.abs(carX)>220){ damage+=dt*Math.max(.35f,2.0f- tires*.35f); speed*=Math.max(.97f,1f-dt*.55f); }
    if(Math.abs(carX)>170 && speed>18){ damage+=dt*.12f; }
    if(Math.abs(carX)>170 && !edgeWarned){ edgeWarned=true; haptic(55); }
    if(Math.abs(carX)<145) edgeWarned=false; if(distance<380) serviceWarned=false;
    if(distance>=850f && !finishWarned){ finishWarned=true; haptic(120); } if(level==1 && distance>395 && distance<405 && !serviceWarned){serviceWarned=true;sectionPulse=2.2f;haptic(45);} if(level==1 && distance>605 && distance<615){sectionPulse=2.2f;}
    if(distance<nextCoinCheckpoint && nextCoinCheckpoint-distance<=80f && !checkpointWarned){ checkpointWarned=true; haptic(35); }
    // Level 1 skill obstacles: four small rocks force gentle steering without blocking the trail.
    if(level==1){
      // Two soft rut zones make the countryside trail feel uneven without stopping the car.
      if((terrainZone>.24f&&terrainZone<.38f || terrainZone>.68f&&terrainZone<.82f) && speed>10){
        speed *= Math.max(.985f, 1f-dt*.18f);
        cameraShake=Math.max(cameraShake, .8f+speed*.006f);
      }
      int[] od={180,360,540,760};int[] ox={-72,68,-58,82};for(int i=0;i<od.length;i++){if(distance>=od[i] && distance<od[i]+16 && Math.abs(carX-ox[i])<58 && speed>8 && lastObstacleHit!=i){damage+=1.2f;speed*=.78f;suspension+=1.7f;cameraShake=Math.max(cameraShake,3.2f);lastObstacleHit=i;haptic(35);pickupPulse=Math.max(pickupPulse,.8f);}}}
    // Large deployment: varied trail hazards and a forgiving fuel reserve on checkpoints.
    if(level>1){ int[] hd={140,310,520,710,875}; for(int i=0;i<hd.length;i++){ if(distance>=hd[i] && distance<hd[i]+14 && Math.abs(carX-(float)Math.sin(i*2.7+level)*75)<52 && speed>9 && lastObstacleHit!=100+i){ damage+=0.8f; speed*=.84f; suspension+=1.2f; cameraShake=Math.max(cameraShake,2.4f); lastObstacleHit=100+i; haptic(28); } } }
    if(damage>100)damage=100; if(fuel<=0.0f || damage>=100.0f){ speed*=Math.max(0f,1f-dt*4f); if(!finished){ failed=true; paused=true; speed=0; stopControls(); } }
    if(distance>=1000 && !finished){ finished=true; paused=true; speed=0; int stars=(damage<25&&fuel>35)?3:(damage<60&&fuel>15)?2:1; winStreak++; int finishReward=50+(winStreak*5)+((stars-1)*10); coins+=finishReward; playerXp+=100+(stars*25); if(missionDone()){missionReward=40; coins+=40; playerXp+=50;} if(winStreak>=3){coins+=75; playerXp+=100; milestoneTimer=3.5f;} if(level<20) unlocked=Math.max(unlocked,level+1); persistProgress(); }
  }
  float baseSpeed(){return Math.max(22f,48-level*1.4f+engine*4.5f+tires*1.5f);}
  String vehicleName(){return new String[]{"TRAILER 4x4","RANGER X","MUDMASTER"}[selectedVehicle];}
  String vehicleRole(){return new String[]{"BALANCED • STARTER","FAST • HIGH FUEL USE","GRIP • SLOWER TOP SPEED"}[selectedVehicle];}
  String weather(){return new String[]{"CLEAR","WINDY","DUSTY","RAIN","FOG","ROCKY","MUD","STORM","NIGHT","SUNSET","CANYON","RIVERBED","PINE","RED ROCK","SNOW","COAST","JUNGLE","DESERT","PEAK NIGHT","MOONLIGHT"}[level-1];}
  void drawGame(Canvas c){
    int sky=skies[level-1], ground=grounds[level-1]; float horizon=H*.39f;
    // V122 visual deployment: layered sky gradient gives the scene more depth than a flat fill.
    int skyTop=sky, skyBottom=Color.rgb(Math.max(20,Color.red(sky)*3/5),Math.max(30,Color.green(sky)*3/5),Math.max(35,Color.blue(sky)*3/5));
    p.setShader(new LinearGradient(0,0,0,horizon,skyTop,skyBottom,Shader.TileMode.CLAMP)); c.drawRect(0,0,W,horizon,p); p.setShader(null);
    // Atmosphere: sun, clouds and layered mountains for stronger depth.
    p.setStyle(Paint.Style.FILL); p.setColor(0x55fff0b0); c.drawCircle(W*.78f,H*.16f,Math.min(W,H)*.09f,p);
    for(int i=0;i<5;i++){float cx=((i*211+distance*.08f)%(W+180))-90;float cy=H*(.10f+(i%3)*.045f);p.setColor(0x35ffffff);c.drawOval(cx,cy,cx+120,cy+24,p);c.drawOval(cx+45,cy-12,cx+175,cy+24,p);}
    p.setColor(ground);c.drawRect(0,horizon,W,H,p);
    // Three mountain layers create a wider 3D world.
    drawMountains(c,horizon,0x44364f55,110,1.10f);
    drawMountains(c,horizon,0x66506461,82,1.55f);
    drawMountains(c,horizon,level>=9?0xff172337:0xff405a54,62,2.10f);
    // V122 distant snow/rock highlights and atmospheric haze improve the perceived 3D depth.
    if(level==1 || level==6 || level==8 || level==15 || level>=19){
      p.setColor(0x66e8eee9); Path snow=new Path(); snow.moveTo(W*.18f,horizon-55); snow.lineTo(W*.28f,horizon-115); snow.lineTo(W*.37f,horizon-58); snow.lineTo(W*.48f,horizon-132); snow.lineTo(W*.60f,horizon-62); snow.lineTo(W*.73f,horizon-108); snow.lineTo(W*.84f,horizon-55); snow.lineTo(W*.84f,horizon); snow.lineTo(W*.18f,horizon); snow.close(); c.drawPath(snow,p);
    }
    p.setColor(0x182f4a42); c.drawRect(0,horizon-8,W,horizon+34,p);
    // Road with broad shoulders and perspective.
    float cx=W/2f+carX*.16f+(float)Math.sin(distance*.006f)*18f; float roadTop=45, roadBottom=Math.min(W*.74f,430);
    Path shoulder=new Path(); shoulder.moveTo(cx-roadTop-26,horizon);shoulder.lineTo(cx+roadTop+26,horizon);shoulder.lineTo(W/2f+roadBottom+34,H);shoulder.lineTo(W/2f-roadBottom-34,H);shoulder.close();p.setColor(0xff665440);c.drawPath(shoulder,p);
    Path road=new Path(); road.moveTo(cx-roadTop,horizon);road.lineTo(cx+roadTop,horizon);road.lineTo(W/2f+roadBottom,H);road.lineTo(W/2f-roadBottom,H);road.close();p.setColor((level==1 && distance>400 && distance<520)?0xff5f5140:(level==1 && distance>600 && distance<700)?0xff6b6255:0xff795e46);c.drawPath(road,p);
    // Level 1 route surfaces: muddy ruts and a wooden bridge are visible, not just HUD labels.
    if(level==1 && distance>400 && distance<520){
      for(int k=0;k<7;k++){float tt=k/7f; float yy=horizon+tt*(H-horizon)*.78f; float hh=roadTop+(roadBottom-roadTop)*tt; p.setColor(0x33402f24); c.drawOval(W/2f-hh*.48f,yy,W/2f-hh*.22f,yy+10+tt*18,p); c.drawOval(W/2f+hh*.22f,yy,W/2f+hh*.48f,yy+10+tt*18,p);}
    }
    if(level==1 && distance>600 && distance<700){
      for(int k=0;k<12;k++){float tt=k/12f; float yy=horizon+tt*(H-horizon); float hh=roadTop+(roadBottom-roadTop)*tt; p.setColor(0x775c4938); c.drawRect(W/2f-hh*.82f,yy,W/2f+hh*.82f,yy+3+tt*5,p);}
    }
    // Road bands / tire wear / center markings + subtle 3D rut shading.
    for(int band=0;band<16;band++){float t=((band/16f+roadPhase*.010f)%1f);float y=horizon+t*(H-horizon);float half=roadTop+(roadBottom-roadTop)*t;p.setColor((band%2==0)?0x10120e0a:0x08000000);c.drawRect(W/2f-half,y,W/2f+half,y+3+7*t,p);}
    stroke.setStrokeWidth(5);stroke.setColor(0xffb89a70);Path edge=new Path();edge.moveTo(cx-roadTop,horizon);edge.lineTo(W/2f-roadBottom,H);c.drawPath(edge,stroke);edge.reset();edge.moveTo(cx+roadTop,horizon);edge.lineTo(W/2f+roadBottom,H);c.drawPath(edge,stroke);
    for(int i=0;i<10;i++){float t=((i/10f+roadPhase*.012f)%1f);float y=horizon+t*(H-horizon);float half=roadTop+(roadBottom-roadTop)*t;float mw=3+13*t;p.setColor(0xffe0c987);c.drawRoundRect(W/2f-mw,y,W/2f+mw,y+5+10*t,3,3,p);}
    stroke.setStrokeWidth(1.5f);
    for(int i=0;i<16;i++){float t=((i*.097f+roadPhase*.006f)%1f);float y=horizon+t*(H-horizon);float half=roadTop+(roadBottom-roadTop)*t;float drift=(float)Math.sin(i*2.7+distance*.01)*half*.55f;stroke.setColor(0x332b2119);c.drawLine(W/2f+drift-half*.42f,y,W/2f+drift-half*.18f,y+2+7*t,stroke);c.drawLine(W/2f+drift+half*.18f,y,W/2f+drift+half*.42f,y+2+7*t,stroke);}
    // Roadside grass, trees and stones.
    for(int i=0;i<30;i++){float t=((i*.037f+roadPhase*.003f)%1f);float y=horizon+t*(H-horizon);float half=roadTop+(roadBottom-roadTop)*t;float side=i%2==0?-1:1;float x=W/2f+side*(half+18+((i*31)%75));float sz=7+34*t;p.setColor(0x66334d2b);c.drawOval(x-sz,y+sz*.25f,x+sz,y+sz*.55f,p);p.setColor(0xff3f713d);for(int g=0;g<4;g++){float gx=x+(g-1.5f)*sz*.35f;c.drawLine(gx,y+sz*.45f,gx+(g%2==0?-3:3),y-sz*.25f,stroke);}}
    for(int i=0;i<20;i++){float t=((i*.051f+roadPhase*.004f)%1f);float y=horizon+t*(H-horizon);float half=roadTop+(roadBottom-roadTop)*t;float side=i%2==0?-1:1;float x=W/2f+side*(half+38+((i*19)%68));float sz=18+65*t;p.setColor(0x33000000);c.drawOval(x-sz*.72f,y+2,x+sz*.72f,y+sz*.22f,p);p.setColor(0xff59412e);c.drawRoundRect(x-sz*.10f,y-sz*.05f,x+sz*.10f,y+sz*.62f,sz*.08f,sz*.08f,p);p.setColor(0xff21482f);c.drawCircle(x,y-sz*.62f,sz*.48f,p);p.setColor(0xff326b3b);c.drawCircle(x-sz*.30f,y-sz*.48f,sz*.36f,p);p.setColor(0xff438248);c.drawCircle(x+sz*.28f,y-sz*.46f,sz*.34f,p);p.setColor(0xff55934f);c.drawCircle(x,y-sz*.82f,sz*.28f,p);}
    // Obstacles / boulders.
    for(int i=0;i<7;i++){float t=((i*.17f+roadPhase*.006f)%1f);float y=horizon+t*(H-horizon);float half=roadTop+(roadBottom-roadTop)*t;float x=W/2f+(float)Math.sin(i*4.1+distance*.018)*half*.62f;float sz=7+13*t;p.setColor(0xff51453a);c.drawOval(x-sz*1.2f,y-sz*.4f,x+sz*1.2f,y+sz*.55f,p);p.setColor(0x447f7368);c.drawOval(x-sz*.55f,y-sz*.35f,x+sz*.25f,y,p);}
    // Level 1 visible tire ruts on the countryside trail.
    if(level==1){
      for(int side=-1;side<=1;side+=2){
        Path rut=new Path();
        for(int j=0;j<9;j++){float t=j/8f; float y=horizon+(t*t)*(H-horizon); float half=roadTop+(roadBottom-roadTop)*t; float x=W/2f+side*half*.34f; if(j==0) rut.moveTo(x,y); else rut.lineTo(x,y);}
        stroke.setStrokeWidth(7); stroke.setColor(0x30261e18); c.drawPath(rut,stroke);
      }
    }
    // Level 1 roadside scenery: simple perspective shrubs/trees make the trail easier to read without blocking the road.
    if(level==1){
      for(int i=0;i<12;i++){
        float t=((i*.083f+roadPhase*.004f)%1f);
        float y=horizon+t*(H-horizon);
        float half=roadTop+(roadBottom-roadTop)*t;
        float side=(i%2==0)?-1f:1f;
        float x=W/2f+side*(half+28+18*t)+(float)Math.sin(i*2.1+distance*.004f)*10f;
        float h=14+42*t;
        p.setColor(0x553b2f24); c.drawOval(x-h*.34f,y-h*.08f,x+h*.34f,y+h*.18f,p);
        p.setColor(0xff4f5b43); c.drawCircle(x,y-h*.55f,h*.42f,p);
        p.setColor(0xff687354); c.drawCircle(x-h*.18f,y-h*.72f,h*.28f,p);
      }
    }

    // Level 1 skill rocks: the same four rocks used by collision physics are rendered in perspective, so the player can react before impact.
    if(level==1){int[] od={180,360,540,760};int[] ox={-72,68,-58,82};for(int i=0;i<od.length;i++){float d=od[i]-distance;if(d>0&&d<260){float z=1f-d/260f;float y=horizon+z*(H-horizon)*.70f;float half=roadTop+(roadBottom-roadTop)*z;float x=W/2f+ox[i]*(.20f+.80f*z);float sz=6f+30f*z;p.setColor(0x55000000);c.drawOval(x-sz*1.15f,y+sz*.42f,x+sz*1.15f,y+sz*.72f,p);p.setColor(0xff5a5149);c.drawOval(x-sz,y-sz*.55f,x+sz,y+sz*.50f,p);p.setColor(0xff82766b);c.drawOval(x-sz*.52f,y-sz*.38f,x+sz*.18f,y-sz*.02f,p);}}}
    // Driving guide: a subtle center-line target and a clear off-road warning help the player read the trail on a phone screen.
    float guideX=W/2f+(float)Math.sin(distance*.006f)*18f;
    if(Math.abs(carX)>150 && speed>2){
      int alpha=(int)(115+70*Math.abs(Math.sin(distance*.08f)));
      txt(c,"KEEP ON ROAD",W/2f,H*.29f,15,Color.argb(alpha,255,224,92),Paint.Align.CENTER);
      stroke.setStrokeWidth(4); stroke.setColor(Color.argb(alpha,255,224,92));
      c.drawLine(W/2f+carX*.16f,H*.67f,guideX,H*.54f,stroke);
    }
    if(level==1 && introTimer<=0 && distance<180 && !finished && !failed){
      p.setColor(0x99101820); c.drawRoundRect(W*.30f,H*.20f,W*.70f,H*.27f,12,12,p);
      txt(c,"LEVEL 1 • FOLLOW THE TRAIL TO 1000 m",W/2f,H*.245f,12,Color.WHITE,Paint.Align.CENTER);
    }
    // Roadside signposts and reflective markers create a recognizable trail identity.
    for(int i=0;i<8;i++){float t=((i*.13f+roadPhase*.0045f)%1f);float y=horizon+t*(H-horizon);float half=roadTop+(roadBottom-roadTop)*t;float side=(i%2==0?-1:1);float x=W/2f+side*(half+24+((i*37)%55));float sh=18+42*t;stroke.setStrokeWidth(2+2*t);stroke.setColor(0xff4b3b2d);c.drawLine(x,y,x,y-sh,stroke);round(c,x-16*t,y-sh-10*t,x+16*t,y-sh+1*t,3, i%3==0?0xffd6a83d:0xffe6e8df);txt(c,i%3==0?"TRAIL":"RIDGE",x,y-sh-2*t,4+3*t,0xff263238,Paint.Align.CENTER);}
    // Sunlit dust motes / terrain particles.
    for(int i=0;i<22;i++){float px=(i*149+distance*1.1f)%W;float py=horizon+20+(i*47)%(int)(H*.55f);p.setColor(0x18fff2cf);c.drawCircle(px,py,1+(i%3),p);}
    // V122 Level 1 landmark pass: bridge rails, water crossing and mud splashes are tied to the route distance.
    if(level==1){
      if(distance>585 && distance<735){
        float z=Math.max(.15f,1f-Math.abs(660-distance)/150f); float by=horizon+(.72f*z)*(H-horizon); float bw=roadTop+(roadBottom-roadTop)*(.72f*z);
        p.setColor(0xff4f7d82); c.drawRect(W/2f-bw*.95f,by+8,W/2f+bw*.95f,by+28+z*20,p);
        stroke.setStrokeWidth(4); stroke.setColor(0xff8b6848); c.drawLine(W/2f-bw*.9f,by-2,W/2f-bw*.9f,by+42+z*15,stroke); c.drawLine(W/2f+bw*.9f,by-2,W/2f+bw*.9f,by+42+z*15,stroke);
        for(int j=-3;j<=3;j++){float px=W/2f+j*bw*.28f; c.drawLine(px,by-4,px,by+18+z*12,stroke);}
      }
      if(distance>395 && distance<545){
        float z=.35f; for(int j=0;j<9;j++){float px=W/2f+(j-4)*24; float py=H*.72f+(j%3)*13; p.setColor(0x55382a20); c.drawCircle(px,py,5+(j%3)*2,p);} txt(c,"MUD + RUTS",W/2f,H*.37f,10,0xffffd45c,Paint.Align.CENTER);
      }
      if(speed>12 && distance>395 && distance<545){ for(int j=0;j<8;j++){float px=W/2f+carX+(j-4)*18; float py=H*.80f+(j%2)*7; p.setColor(0x55a98a67); c.drawCircle(px,py,3+(j%3),p);} }
    }
    // V125 recovery markers: pickups are now visible in the world, not only in the HUD.
    if(level==1){
      int[] pd={320,680}; int[] px={72,-76};
      for(int i=0;i<2;i++){float d=pd[i]-distance; if(!fuelPickupTaken[i] && d>-20 && d<220){float z=1f-Math.max(0,d)/220f; float yy=horizon+z*(H-horizon)*.72f; float half=roadTop+(roadBottom-roadTop)*z; float x=W/2f+px[i]*(.18f+.82f*z); float r=7+12*z; p.setColor(0x55000000);c.drawCircle(x+2,yy+8,r+2,p);p.setColor(0xffffc83d);c.drawCircle(x,yy,r,p);p.setColor(0xff263238);c.drawRect(x-2,yy-r*.65f,x+2,yy+r*.65f,p); txt(c,"FUEL",x,yy-r-5,7+z*2,0xffffd45c,Paint.Align.CENTER);}}
      int[] rd={470,820}; int[] rx={-82,78};
      for(int i=0;i<2;i++){float d=rd[i]-distance; if(!repairPickupTaken[i] && d>-20 && d<220){float z=1f-Math.max(0,d)/220f; float yy=horizon+z*(H-horizon)*.72f; float half=roadTop+(roadBottom-roadTop)*z; float x=W/2f+rx[i]*(.18f+.82f*z); float r=7+12*z; p.setColor(0x55000000);c.drawCircle(x+2,yy+8,r+2,p);p.setColor(0xff48c7a3);c.drawCircle(x,yy,r,p);p.setColor(Color.WHITE);c.drawRect(x-2,yy-r*.55f,x+2,yy+r*.55f,p);c.drawRect(x-r*.55f,yy-2,x+r*.55f,yy+2,p);txt(c,"REPAIR",x,yy-r-5,7+z*2,0xff8ff1d0,Paint.Align.CENTER);}}
    }
    // Route difficulty banner makes each Level 1 section visually readable before the handling changes.
    if(level==1 && distance>360 && distance<760 && !paused && !finished && !failed){String label=distance<560?"MUD ZONE • REDUCE SPEED":"WOOD BRIDGE • KEEP CENTER";txt(c,label,W/2f,H*.285f,12,0xffffd45c,Paint.Align.CENTER);}
    // Checkpoint gate.
    float[] gates={250f,500f,750f,1000f}; for(float gd:gates){float d=gd-distance; if(d>0&&d<125){float z=1f-d/125f; float gateY=horizon+(1f-z)*H*.42f; float gh=42+z*42; float half=70+z*80; stroke.setStrokeWidth(4+z*2);stroke.setColor(0xffffd45c);c.drawLine(W/2f-half,gateY,W/2f-half,gateY-gh,stroke);c.drawLine(W/2f+half,gateY,W/2f+half,gateY-gh,stroke);c.drawLine(W/2f-half,gateY-gh,W/2f+half,gateY-gh,stroke);txt(c,gd>=1000?"FINISH":"CHECKPOINT",W/2f,gateY-gh-8,12+z*3,0xffffd45c,Paint.Align.CENTER);}}
    c.save(); c.translate((float)Math.sin(distance*.05f)*cameraShake, (float)Math.cos(distance*.045f)*cameraShake*.6f);
    if(speed>12 && (level==6||level==11||level==14)){ p.setColor(0x22ffffff); for(int r=0;r<3;r++){float yy=H*.80f+72+r*9; c.drawOval(W/2f+carX-72+r*9,yy,W/2f+carX-42+r*9,yy+5,p); c.drawOval(W/2f+carX+42-r*9,yy,W/2f+carX+72-r*9,yy+5,p);} }
    // Level 1 driving cue: a subtle center marker keeps the road/car relationship clear.
    if(level<=2 && speed>1){stroke.setStrokeWidth(2);stroke.setColor(0x55fff0a0);float cueX=W/2f+carX*.16f;c.drawLine(cueX,H*.78f,cueX,H*.70f,stroke);}
    if(level==1 && speed>1){
      // Two short perspective guide strips visually connect the SUV to the trail.
      for(int g=0;g<4;g++){float gt=(g+1)/5f;float gy=horizon+gt*(H-horizon);float gh=roadTop+(roadBottom-roadTop)*gt;float gx=W/2f+carX*.16f+(float)Math.sin(distance*.006f)*18f*(1f-gt);float gw=3+7*gt; p.setColor(0x55f7d77a);c.drawRoundRect(gx-gw,gy,gx+gw,gy+4+5*gt,3,3,p);}
    }
    drawCar(c,W/2f+carX,H*.80f); c.restore();
    // Small reflective puddles on wet stages.
    if(level==4||level==8){for(int i=0;i<9;i++){float t=((i*.11f+roadPhase*.003f)%1f);float py=horizon+t*(H-horizon);float half=roadTop+(roadBottom-roadTop)*t;float px=W/2f+(float)Math.sin(i*3.1+distance*.01)*half*.45f;float pw=7+20*t;p.setColor(0x447fa8b0);c.drawOval(px-pw,py,px+pw,py+3+5*t,p);}}
    // Level 1 terrain sections: visible landmarks match the handling changes, so the player learns the trail instead of guessing.
    if(level==1){
      if(distance<560){ float d=420-distance; if(d>-80&&d<150){ float z=1f-Math.max(0,d)/150f; float yy=horizon+z*(H-horizon)*.62f; float ww=45+z*150; round(c,W/2f-ww,yy-10,W/2f+ww,yy+18,8,0xff6f5840); txt(c,"MUD",W/2f,yy+3,10,0xffffd45c,Paint.Align.CENTER); } }
      if(distance<760){ float d=640-distance; if(d>-80&&d<150){ float z=1f-Math.max(0,d)/150f; float yy=horizon+z*(H-horizon)*.62f; float ww=45+z*150; stroke.setStrokeWidth(7); stroke.setColor(0xff8b8274); c.drawLine(W/2f-ww,yy,W/2f+ww,yy,stroke); c.drawLine(W/2f-ww,yy+18,W/2f+ww,yy+18,stroke); txt(c,"WOOD BRIDGE",W/2f,yy-8,10,0xffffd45c,Paint.Align.CENTER); } }
      if(distance>820 && distance<930){ txt(c,"FINAL CLIMB",W/2f,H*.30f,13,0xffffd45c,Paint.Align.CENTER); }
    }
    if(servicePulse>0){ txt(c,"TRAIL SERVICE  •  +FUEL  +REPAIR",W/2f,H*.25f,14,0xffffd45c,Paint.Align.CENTER); }
    if(sectionPulse>0 && level==1){ String sec=(distance<560?"MUD SECTION":"WOOD BRIDGE SECTION"); txt(c,sec,W/2f,H*.33f,12,0xfff0d18a,Paint.Align.CENTER); }
    // Weather layers.
    if(level==4||level==8){stroke.setStrokeWidth(2);stroke.setColor(0x88d9e8f0);for(int i=0;i<90;i++){float x=(i*97+distance*4)%W,y=(i*53+distance*8)%H;c.drawLine(x,y,x-6,y+18,stroke);}}
    if(level==3||level==7){p.setColor(0x25d9a86d);c.drawRect(0,horizon,W,H,p);for(int i=0;i<24;i++){float x=(i*83+distance*1.5f)%W,y=horizon+35+(i*37)%180;p.setColor(0x22fff0cf);c.drawCircle(x,y,5+(i%4)*2,p);}}
    if(level==5){p.setColor(0x44eef5f5);c.drawRect(0,horizon-20,W,H*.72f,p);}
    if(level==9){p.setColor(0xff182139);c.drawCircle(W*.82f,H*.17f,32,p);for(int i=0;i<45;i++){p.setColor(0xaaFFFFFF);float sx=(i*137)%W,sy=(i*71)%(int)(H*.36f);c.drawCircle(sx,sy,1.3f,p);}}
    // Subtle cinematic vignette keeps the vehicle and trail visually dominant on small screens.
    p.setShader(new LinearGradient(0,0,0,H,0x22000000,0x00000000,Shader.TileMode.CLAMP)); c.drawRect(0,0,W,H,p); p.setShader(null);
    drawHud(c);
    if(!finished && !failed && damage>55){
      p.setColor((damage>80)?0x32ff4f3f:0x1dffb35f); c.drawRect(0,0,W,H,p);
      txt(c,damage>80?"CRITICAL DAMAGE":"VEHICLE DAMAGE",W/2f,H*.34f,16,damage>80?0xffff8c78:0xffffd45c,Paint.Align.CENTER);
    }
    if(finished){p.setColor(0xdd081017);c.drawRect(0,0,W,H,p); int stars=(damage<25&&fuel>35)?3:(damage<60&&fuel>15)?2:1; String starText=stars==3?"★★★":stars==2?"★★☆":"★☆☆"; txt(c,"🏁 LEVEL COMPLETE",W/2,H*.29f,30,0xffffd45c,Paint.Align.CENTER); txt(c,starText,W/2,H*.39f,28,0xffffd45c,Paint.Align.CENTER);txt(c,"+"+(50+(winStreak*5)+(((damage<25&&fuel>35)?3:(damage<60&&fuel>15)?2:1)-1)*10)+" COINS",W/2,H*.47f,20,Color.WHITE,Paint.Align.CENTER);txt(c,"DISTANCE 1000 m  •  DAMAGE "+(int)damage+"%  •  FUEL "+(int)fuel+"%",W/2,H*.515f,12,0xffcbd7dd,Paint.Align.CENTER); txt(c,"XP +"+(100+(stars*25))+(missionDone()?"  •  MISSION +50 XP":""),W/2,H*.55f,11,0xffffd45c,Paint.Align.CENTER); txt(c,vehicleName()+"  •  ENGINE "+engine+"  •  TIRES "+tires,W/2,H*.585f,10,0xffb9c9d0,Paint.Align.CENTER);txt(c,level<20?"NEXT LEVEL UNLOCKED":"ALL 20 LEVELS COMPLETE",W/2,H*.56f,15,0xffcbd7dd,Paint.Align.CENTER);button(c,W*.18f,H*.62f,W*.48f,H*.74f,level<20?"NEXT":"LEVELS",0xff26734a);button(c,W*.52f,H*.62f,W*.82f,H*.74f,"REPLAY",0xff455864);}
    else if(failed){p.setColor(0xe6081017);c.drawRect(0,0,W,H);txt(c,"RUN FAILED",W/2,H*.31f,30,0xffff8c78,Paint.Align.CENTER);String reason=fuel<=0?"OUT OF FUEL":"VEHICLE DAMAGED";txt(c,reason,W/2,H*.40f,18,Color.WHITE,Paint.Align.CENTER);txt(c,"DISTANCE "+(int)distance+" m  •  DAMAGE "+(int)damage+"%",W/2,H*.47f,12,0xffcbd7dd,Paint.Align.CENTER);txt(c,"TRY AGAIN TO KEEP YOUR PROGRESS",W/2,H*.53f,12,0xff9fb0b8,Paint.Align.CENTER);button(c,W*.20f,H*.62f,W*.48f,H*.74f,"RETRY",0xff26734a);button(c,W*.52f,H*.62f,W*.80f,H*.74f,"LEVELS",0xff455864);}
    else if(paused){p.setColor(0xb8081017);c.drawRect(0,0,W,H);txt(c,"PAUSED",W/2,H*.34f,30,0xffffd45c,Paint.Align.CENTER);txt(c,"Tap RESUME to continue",W/2,H*.43f,14,Color.WHITE,Paint.Align.CENTER);button(c,W*.36f,H*.52f,W*.64f,H*.64f,"RESUME",0xff26734a);}
    else if(!paused && !finished && introTimer<=0 && speed<1f){p.setColor(0x66303d44);c.drawRoundRect(W*.32f,H*.50f,W*.68f,H*.57f,12,12,p);txt(c,"HOLD ACCEL TO START • USE ◀ ▶ TO STEER",W/2,H*.545f,13,Color.WHITE,Paint.Align.CENTER);}
    if(!paused && !finished && introTimer>0){p.setColor(0xaa081017);c.drawRect(0,H*.25f,W,H*.47f,p);txt(c,"LEVEL "+level,W/2,H*.34f,28,0xffffd45c,Paint.Align.CENTER);txt(c,names[level-1],W/2,H*.41f,18,Color.WHITE,Paint.Align.CENTER);txt(c,"REACH 1000 m",W/2,H*.46f,11,0xffd6e0e5,Paint.Align.CENTER);}
    if(level==1 && !paused && !finished && !failed && introTimer<=0 && distance<90){txt(c,"STEER ◀ / ▶   •   ACCEL TO MOVE",W/2,H*.64f,11,0xfff0f4f5,Paint.Align.CENTER);}
  }
  void drawMountains(Canvas c,float horizon,int color,float height,float phase){Path m=new Path();m.moveTo(0,horizon);for(int i=0;i<=12;i++){float x=i*W/12f;float y=horizon-height*(.35f+.65f*(float)Math.abs(Math.sin(i*phase+level*.37)));m.lineTo(x,y);}m.lineTo(W,horizon);m.close();p.setColor(color);c.drawPath(m,p);}
  void drawCar(Canvas c,float x,float y){float bob=(float)Math.sin(suspension)*3 + (float)Math.sin(distance*.08)*2; y+=bob;
    // Steering body lean makes the vehicle motion easier to read on a phone screen.
    c.save(); c.rotate(steeringVisual*7.5f,x,y+18); y += (float)Math.sin(suspension*1.7f)*1.8f;
    // Ground shadow and dust/mud trail.
    p.setColor(0x66000000);c.drawOval(x-78,y+66,x+78,y+86,p);
    if(speed>8){for(int i=0;i<7;i++){float dx=(i-3)*24-(float)Math.sin(distance*.08+i)*12,dy=72+(i%3)*7;p.setColor(0x38d5c09b);c.drawCircle(x+dx, y+dy, 4+(i%3)*2,p);}}
    if((brake||Math.abs(steer)>.75f) && speed>14){stroke.setStrokeWidth(3);stroke.setColor(0x55302720);c.drawLine(x-50,y+63,x-42,y+92,stroke);c.drawLine(x+50,y+63,x+42,y+92,stroke);}
    // Wheels with hubs.
    p.setColor(0xff111416);c.drawRoundRect(x-64,y-5,x-38,y+72,11,11,p);c.drawRoundRect(x+38,y-5,x+64,y+72,11,11,p);
    p.setColor(0xff6f7779);c.drawOval(x-59,y+17,x-43,y+54,p);c.drawOval(x+43,y+17,x+59,y+54,p);p.setColor(0xff20272a);c.drawCircle(x-51,y+36,5,p);c.drawCircle(x+51,y+36,5,p);
    // Front-wheel steering angle gives a stronger sense of vehicle control.
    c.save(); c.rotate(steeringVisual*24f,x-51,y+36); p.setColor(0xff15191b); c.drawRoundRect(x-59,y+14,x-43,y+57,5,5,p); c.restore();
    c.save(); c.rotate(steeringVisual*24f,x+51,y+36); p.setColor(0xff15191b); c.drawRoundRect(x+43,y+14,x+59,y+57,5,5,p); c.restore();
    // SUV body with fenders. Vehicle paint changes by chapter so levels feel distinct.
    int[] paints={0xffb94730,0xff2e5f7b,0xff6b6f42,0xff8a3f32,0xff4d5968,0xff7b5b38,0xff365c46,0xff5a465f,0xff263b61,0xffa85f3f};
    int bodyPaint=paints[(level-1)%paints.length];
    p.setColor(bodyPaint);Path b=new Path();b.moveTo(x-55,y+7);b.lineTo(x-47,y-48);b.quadTo(x,y-80,x+47,y-48);b.lineTo(x+55,y+7);b.lineTo(x+48,y+58);b.lineTo(x-48,y+58);b.close();c.drawPath(b,p);
    p.setColor(0x44ffffff);c.drawPath(new Path(){ {moveTo(x-44,y+2);lineTo(x-37,y-42);quadTo(x-5,y-62,x+5,y-62);lineTo(x+5,y+50);lineTo(x-42,y+50);close();} },p);
    // Split windshield / side glass.
    p.setColor(0xff91c5d3);Path w1=new Path();w1.moveTo(x-34,y-49);w1.lineTo(x-3,y-58);w1.lineTo(x-3,y-20);w1.lineTo(x-29,y-20);w1.close();c.drawPath(w1,p);Path w2=new Path();w2.moveTo(x+3,y-58);w2.lineTo(x+34,y-49);w2.lineTo(x+29,y-20);w2.lineTo(x+3,y-20);w2.close();c.drawPath(w2,p);
    stroke.setColor(0xffe8b95d);stroke.setStrokeWidth(2.5f);c.drawPath(w1,stroke);c.drawPath(w2,stroke);
    // Grille, headlights and bumper.
    p.setColor(0xff20282b);c.drawRoundRect(x-39,y+5,x+39,y+30,6,6,p);p.setColor(0xffffe27c);c.drawRoundRect(x-31,y-2,x-18,y+7,3,3,p);c.drawRoundRect(x+18,y-2,x+31,y+7,3,3,p);
    // Night stages get a soft headlight beam so the trail remains readable.
    if(level==9 || level==20){p.setColor(0x18fff4b0);Path beam=new Path();beam.moveTo(x-28,y+6);beam.lineTo(x-92,y+92);beam.lineTo(x-12,y+92);beam.close();c.drawPath(beam,p);beam.reset();beam.moveTo(x+28,y+6);beam.lineTo(x+92,y+92);beam.lineTo(x+12,y+92);beam.close();c.drawPath(beam,p);}p.setColor(0xffe5e8e8);c.drawRect(x-17,y+13,x+17,y+17,p);p.setColor(0xff171d20);c.drawRoundRect(x-50,y+48,x+50,y+63,5,5,p);
    // Mirrors, roof rack and spare-wheel hint.
    p.setColor(0xff20272a);c.drawRoundRect(x-52,y-24,x-44,y-10,3,3,p);c.drawRoundRect(x+44,y-24,x+52,y-10,3,3,p);stroke.setStrokeWidth(4);stroke.setColor(0xff22282b);c.drawLine(x-38,y-61,x+38,y-61,stroke);c.drawLine(x-28,y-67,x+28,y-67,stroke);p.setColor(0xff20272a);c.drawCircle(x,y+41,10,p);p.setColor(0xff8d9698);c.drawCircle(x,y+41,5,p);
    txt(c,"OFFROAD",x,y+43,10,Color.WHITE,Paint.Align.CENTER);
    c.restore();
  }
  void drawHud(Canvas c){
    round(c,10,10,125,49,10,0xe0101820);txt(c,"LEVEL "+level,20,35,14,Color.WHITE,Paint.Align.LEFT);
    round(c,133,10,270,49,10,0xcc101820);txt(c,"SPEED "+(int)speed,143,35,13,Color.WHITE,Paint.Align.LEFT); round(c,143,40,260,44,2,0x66313f47); p.setColor(0xff6dc58b); c.drawRect(143,40,143+117*Math.min(1f,speed/70f),44,p);
    round(c,278,10,410,49,10,0xcc101820);txt(c,"FUEL "+(int)fuel+"%",288,35,13,0xffbde8c4,Paint.Align.LEFT);
    round(c,W-310,10,W-170,49,10,0xcc101820);txt(c,"COINS "+(int)coins,W-300,35,12,0xffffd45c,Paint.Align.LEFT); round(c,W-165,10,W-10,49,10,0xcc101820);txt(c,names[level-1],W-155,35,12,Color.WHITE,Paint.Align.LEFT);
    round(c,10,56,250,64,4,0x66101720);p.setColor(0xff68c77a);c.drawRect(10,56,10+240*fuel/100f,64,p);
    round(c,10,70,250,78,4,0x66101720);p.setColor(damage>70?0xffd95745:0xffe1ad52);c.drawRect(10,70,10+240*damage/100f,78,p);
    if(level==1 && !finished && !failed){ int[] pd={320,680}; int[] rd={470,820}; for(int i=0;i<2;i++){ int rem=pd[i]-(int)distance; if(!fuelPickupTaken[i] && rem>=0 && rem<=110){ txt(c,"FUEL CAN  "+rem+" m",W/2f,H*.35f+i*16,10,0xffffd45c,Paint.Align.CENTER); } int rr=rd[i]-(int)distance; if(!repairPickupTaken[i] && rr>=0 && rr<=110){ txt(c,"REPAIR KIT  "+rr+" m",W/2f,H*.39f+i*16,10,0xff9fe6b0,Paint.Align.CENTER); } } }
    if(introTimer>0 && distance<1 && !paused){ txt(c,"LEVEL "+level+" • " + names[level-1],W/2f,H*.13f,18,Color.WHITE,Paint.Align.CENTER); txt(c,"ACCELERATE • STEER • REACH 1000 m",W/2f,H*.18f,11,0xffd7e1e5,Paint.Align.CENTER); } txt(c,"DAMAGE",10,93,10,0xffe8c9bd,Paint.Align.LEFT); txt(c,vehicleName()+"  •  E"+engine+" T"+tires,W/2f,61,9,0xffb9c9d0,Paint.Align.CENTER); String status=fuel<=0?"OUT OF FUEL":damage>=100?"VEHICLE DAMAGED":fuel<20?"LOW FUEL":damage>70?"DANGER • DAMAGE":Math.abs(carX)>170?"KEEP ON ROAD":"GOAL 1000 m"; int statusColor=(fuel<=0||damage>=100)?0xffff8c78:(fuel<20||damage>70||Math.abs(carX)>170)?0xffffd45c:0xffd8e2e6; txt(c,status,W/2f,93,10,statusColor,Paint.Align.CENTER); if(checkpointPulse<.8f){txt(c,"CHECKPOINT +1 COIN",W/2f,111,9,0xffffd45c,Paint.Align.CENTER);}txt(c,(int)distance+" / 1000 m",W-72,77,12,Color.WHITE,Paint.Align.CENTER); txt(c,"BEST "+(int)bestDistance+" m",W/2f,77,10,0xffa9bbc4,Paint.Align.CENTER); txt(c,"REMAINING "+Math.max(0,1000-(int)distance)+" m",W/2f,122,10,0xffcbd7dd,Paint.Align.CENTER); if(distance<1000){int checkpointRemaining=Math.max(0,(int)nextCoinCheckpoint-(int)distance); txt(c,"NEXT CHECKPOINT "+checkpointRemaining+" m",W/2f,136,9,0xffb9c9d0,Paint.Align.CENTER); if(checkpointRemaining<=20 && checkpointRemaining>0){txt(c,"CHECKPOINT READY",W/2f,166,10,0xffffd45c,Paint.Align.CENTER);} if(checkpointRemaining<=80 && checkpointRemaining>0){txt(c,"CHECKPOINT AHEAD",W/2f,151,10,0xffffd45c,Paint.Align.CENTER);}} if(level==1 && !finished && !failed){ int[] od={180,360,540,760}; int nearest=10000; for(int d:od){int rem=d-(int)distance;if(rem>=0&&rem<nearest)nearest=rem;} if(nearest<=120){txt(c,"ROCK AHEAD  •  STEER",W/2f,H*.28f,15,0xffffd45c,Paint.Align.CENTER);} if(tractionFeedback>.35f){txt(c,"ROUGH GROUND",W/2f,H*.31f,11,0xffe7c28c,Paint.Align.CENTER);} } if(distance>=850&&distance<1000){txt(c,"FINISH IN "+(1000-(int)distance)+" m",W/2f,H*.18f,16,0xffffd45c,Paint.Align.CENTER);} if(milestoneTimer>0){txt(c,"CHECKPOINT +1 COIN",W/2f,H*.22f,18,0xffffd45c,Paint.Align.CENTER);} if(pickupPulse>0){txt(c,"RECOVERY  •  FUEL +18  •  +3 COINS",W/2f,H*.27f,15,0xffffd45c,Paint.Align.CENTER);} if(repairPulse>0){txt(c,"REPAIR KIT  •  DAMAGE -10%  •  +4 COINS",W/2f,H*.31f,14,0xff9fe6b0,Paint.Align.CENTER);}
    float prog=Math.min(1f,(distance%1000f)/1000f);round(c,W*.36f,56,W*.64f,64,4,0x66101720);p.setColor(0xffffd45c);c.drawRect(W*.36f,56,W*.36f+(W*.28f)*prog,64,p);
    // Mission + progression layer: makes each run more than a straight 1000 m drive.
    String mt=missionText(); txt(c,mt,W/2f,H*.18f,10,0xfff2d27a,Paint.Align.CENTER);
    txt(c,"TRAIL TOKENS "+trailTokens+"   COMBO x"+Math.max(1,combo)+"   XP "+playerXp, W/2f,H*.205f,9,0xffc5d6dc,Paint.Align.CENTER); if(winStreak>0) txt(c,"WIN STREAK "+winStreak+(winStreak>=3?" • +75 BONUS":""),W/2f,H*.225f,9,0xffffd45c,Paint.Align.CENTER);
    if(milestoneTimer>0 && save.getInt("dailyKey",0)!=todayKey()){ txt(c,"DAILY REWARD READY  +25 COINS",W/2f,H*.245f,11,0xffffd45c,Paint.Align.CENTER); }
    // Large two-handed mobile controls.
    float by=H-150,pad=12,gap=12,leftW=Math.min(310f,W*.43f),rightW=Math.min(310f,W*.43f);float bw=(leftW-gap)/2f,lx=pad,rx=W-pad-rightW;
    button(c,lx,by,lx+bw,H-12,"◀",0xff182b34);button(c,lx+bw+gap,by,lx+leftW,H-12,"▶",0xff182b34);
    float rw=(rightW-gap)/2f;button(c,rx,by,rx+rw,H-12,"BRAKE",0xff71382f);button(c,rx+rw+gap,by,rx+rightW,H-12,"ACCEL",0xff2d7c4b);
    float nitL=W/2f-76,nitR=W/2f+76;button(c,nitL,H-210,nitR,H-150,"NITRO",0xffa94e29);
    button(c,W-260,92,W-165,128,"LEVEL",0xff253039); button(c,W-158,92,W-16,128,paused?"RESUME":"PAUSE",0xff253039);
    txt(c,"STEER",pad,H-123,11,0xffe3edf1,Paint.Align.LEFT);txt(c,"BRAKE / ACCEL",W-pad,H-123,11,0xffe3edf1,Paint.Align.RIGHT);
  }
  void button(Canvas c,float l,float t,float r,float b,String s,int color){boolean active=(s.equals("ACCEL")&&throttle)||(s.equals("BRAKE")&&brake)||(s.equals("NITRO")&&nitro)||(s.equals("◀")&&left)||(s.equals("▶")&&right);round(c,l,t,r,b,12,active?0xfff0b43c:color); if(active){stroke.setStyle(Paint.Style.STROKE);stroke.setStrokeWidth(3);stroke.setColor(0xffffefb0);c.drawRoundRect(l+2,t+2,r-2,b-2,10,10,stroke);stroke.setStyle(Paint.Style.STROKE);} txt(c,s,(l+r)/2,(t+b)/2+6, s.equals("NITRO")?15:(s.length()>4?15:24),active?0xff182026:Color.WHITE,Paint.Align.CENTER);}
  void drawGarage(Canvas c){
    c.drawColor(0xff0b1217);
    txt(c,"GARAGE",W/2,48,28,0xffffd45c,Paint.Align.CENTER);
    txt(c,"CHOOSE YOUR OFF-ROAD MACHINE",W/2,74,11,0xffc7d4da,Paint.Align.CENTER);
    round(c,18,92,W-18,178,18,0xff17262d);
    float cx=W/2f, cy=132;
    p.setColor(0xff2d3437); c.drawOval(cx-92,cy+20,cx+92,cy+48,p);
    int body=selectedVehicle==0?0xffb86b38:selectedVehicle==1?0xff3d6f8c:0xff4e784c;
    round(c,cx-92,cy-10,cx+92,cy+30,14,body);
    round(c,cx-48,cy-35,cx+48,cy+3,10,0xff202b30);
    p.setColor(0xffa8c4cc); c.drawRoundRect(cx-37,cy-29,cx-5,cy-5,5,5,p); c.drawRoundRect(cx+5,cy-29,cx+37,cy-5,5,5,p);
    p.setColor(0xff171c1f); c.drawCircle(cx-63,cy+30,18,p); c.drawCircle(cx+63,cy+30,18,p);
    txt(c,vehicleName(),W/2,196,20,Color.WHITE,Paint.Align.CENTER);
    txt(c,vehicleRole(),W/2,218,10,0xffffd45c,Paint.Align.CENTER);
    String[] vn={"TRAILER 4x4","RANGER X","MUDMASTER"};
    String[] vd={"Balanced starter • reliable everywhere","Speed focused • faster checkpoints","Maximum grip • best on mud and rocks"};
    for(int i=0;i<3;i++){float l=14+i*(W-28)/3f,r=l+(W-42)/3f; round(c,l,238,r,326,14,i==selectedVehicle?0xff286b4a:0xff1b2a31); txt(c,vn[i],(l+r)/2,262,11,Color.WHITE,Paint.Align.CENTER); txt(c,vd[i],(l+r)/2,282,8,0xffc7d4da,Paint.Align.CENTER); txt(c,i==0?"FREE":(i==1?"UNLOCK: 500 XP":"UNLOCK: 3 WINS"),(l+r)/2,305,9,0xffffd45c,Paint.Align.CENTER); }
    txt(c,"ENGINE",W*.25f,355,12,0xffd9e4e8,Paint.Align.CENTER); txt(c,"TIRES",W*.75f,355,12,0xffd9e4e8,Paint.Align.CENTER);
    txt(c,"LEVEL "+engine+" / 3",W*.25f,378,18,Color.WHITE,Paint.Align.CENTER); txt(c,"LEVEL "+tires+" / 3",W*.75f,378,18,Color.WHITE,Paint.Align.CENTER); for(int i=0;i<3;i++){round(c,W*.11f+i*34,386,W*.11f+i*34+26,392,i<engine?0xffffd45c:0xff3b4b52); round(c,W*.61f+i*34,386,W*.61f+i*34+26,392,i<tires?0xffffd45c:0xff3b4b52);}
    txt(c,"Upgrade: 100 coins",W*.25f,400,9,0xffffd45c,Paint.Align.CENTER); txt(c,"Upgrade: 100 coins",W*.75f,400,9,0xffffd45c,Paint.Align.CENTER);
    round(c,14,418,W-14,452,10,0xff16242a); txt(c,"COINS "+(int)coins+"   •   XP "+playerXp+"   •   WINS "+winStreak,W/2,441,11,0xffffd45c,Paint.Align.CENTER);
    txt(c,"TAP A VEHICLE TO SELECT  •  BACK TO LEVELS",W/2,H-22,10,0xff9fb0b8,Paint.Align.CENTER);
  }
  void drawLevels(Canvas c){c.drawColor(0xff0d151b); p.setColor(0x182f7c55); for(int i=0;i<18;i++){float xx=(i*97)%W, yy=105+(i*53)%Math.max(1,H-130); c.drawCircle(xx,yy,18+(i%4)*8,p);} txt(c,"OFFROAD WITH ME",W/2,55,28,0xffffd45c,Paint.Align.CENTER);txt(c,"CHAPTER 1 • 20 LEVELS",W/2,82,15,0xffc7d4da,Paint.Align.CENTER);txt(c,"XP "+playerXp+"   •   WIN STREAK "+winStreak+"   •   TOTAL "+(int)totalDistance+" m",W/2,100,10,0xffffd45c,Paint.Align.CENTER);for(int i=0;i<20;i++){int col=i%4,row=i/4;float l=18+col*(W-36)/4f,t=105+row*82,r=l+(W-54)/4f,b=t+62;boolean open=i+1<=unlocked;round(c,l,t,r,b,14,open?0xff26734a:0xff293b46);txt(c,"LEVEL "+(i+1),(l+r)/2,t+22,13,Color.WHITE,Paint.Align.CENTER);txt(c,names[i],(l+r)/2,t+41,9,0xffd8e2e6,Paint.Align.CENTER); if(open){txt(c,"BEST "+(int)save.getFloat("best_"+(i+1),0)+"m",(l+r)/2,t+55,8,0xffffd45c,Paint.Align.CENTER);} else {txt(c,"LOCKED",(l+r)/2,t+55,8,0xffa9b5ba,Paint.Align.CENTER);}}button(c,W*.12f,H-78,W*.38f,H-28,"GARAGE",0xff8a5a2b); button(c,W*.41f,H-78,W*.68f,H-28,"DAILY +25",0xff26734a); txt(c,"XP "+playerXp+"  •  STREAK "+winStreak+"  •  "+(int)totalDistance+" m TOTAL",W*.74f,H-48,10,0xff9fb0b8,Paint.Align.CENTER); txt(c,"OFFLINE • DRIVE • EXPLORE • UPGRADE",W/2,H-8,9,0xff6f858f,Paint.Align.CENTER);}
  int leftId=-1,rightId=-1,brakeId=-1,throttleId=-1,nitroId=-1;
  boolean controlHit(float x,float y){return y>H-168;}
  void stopControls(){left=right=brake=throttle=nitro=false;leftId=rightId=brakeId=throttleId=nitroId=-1;}
  void clearPointer(int id){if(id==leftId){left=false;leftId=-1;} if(id==rightId){right=false;rightId=-1;} if(id==brakeId){brake=false;brakeId=-1;} if(id==throttleId){throttle=false;throttleId=-1;} if(id==nitroId){nitro=false;nitroId=-1;}}
  void assignPointer(int id,float x,float y){
    if(y < H-220) return;
    float pad=12,gap=12,leftW=Math.min(310f,W*.43f),rightW=Math.min(310f,W*.43f),bw=(leftW-gap)/2f;
    float lx=pad,rx=W-pad-rightW,rw=(rightW-gap)/2f;
    if(x>=lx && x<lx+bw){left=true;leftId=id;}
    else if(x>=lx+bw+gap && x<lx+leftW){right=true;rightId=id;}
    else if(x>=rx && x<rx+rw){brake=true;brakeId=id;}
    else if(x>=rx+rw+gap && x<=rx+rightW){throttle=true;throttleId=id;}
    else if(x>W/2f-92 && x<W/2f+92 && y>=H-220 && y<H-140){nitro=true;nitroId=id;}
  }
  @Override public boolean onTouchEvent(android.view.MotionEvent e){
    float x=e.getX(),y=e.getY(); int a=e.getActionMasked(); int index=e.getActionIndex(); int id=e.getPointerId(index);
    if(a==MotionEvent.ACTION_DOWN){
      if(garageScreen){
        if(x<120&&y<75){garageScreen=false;levelScreen=true;invalidate();return true;}
        if(y>=238&&y<=326){int slot=(int)(x/((W-28)/3f)); if(slot<0)slot=0; if(slot>2)slot=2; boolean ok=(slot==0)||(slot==1&&playerXp>=500)||(slot==2&&winStreak>=3); if(ok){selectedVehicle=slot; persistProgress(); invalidate();} return true;}
        if(y>H*.60f&&y<H*.77f&&engine<3&&coins>=100){coins-=100;engine++;save.edit().putFloat("coins",coins).putInt("engine",engine).apply();invalidate();return true;}
        if(y>H*.85f&&tires<3&&coins>=100){coins-=100;tires++;save.edit().putFloat("coins",coins).putInt("tires",tires).apply();invalidate();return true;}
        return true;
      }
      if(levelScreen){
        if(y>H-90&&x<W*.40f){garageScreen=true;invalidate();return true;} if(y>H-90&&x>=W*.40f&&x<W*.72f){claimDaily();invalidate();return true;}
        if(y>100&&y<H-90){int col=(int)(x/(W/4f));int row=(int)((y-100)/82f);int n=row*4+col+1;if(n>=1&&n<=20&&n<=unlocked){level=n;levelScreen=false;reset();}invalidate();}
        return true;
      }
      if(y>90&&y<140&&x>W-165){ paused=!paused; last=System.nanoTime(); invalidate(); return true; }
      if(finished && y>H*.58f && y<H*.76f){if(x>W*.50f){reset();}else if(level<20){level++;reset();}else{levelScreen=true;finished=false;}} else if(failed && y>H*.58f && y<H*.76f){if(x>W*.50f){levelScreen=true;failed=false;paused=false;}else{reset();}} else if(paused && y>H*.48f && y<H*.70f){paused=false;last=System.nanoTime();invalidate();return true;} else if(y>80&&y<145&&x>W-260&&x<=W-165){levelScreen=true; stopControls(); invalidate(); return true;}
      if(y>H-220){assignPointer(id,x,y); return true;}
      return true;
    }
    if(a==MotionEvent.ACTION_POINTER_DOWN){assignPointer(id,x,y); return true;}
    if(a==MotionEvent.ACTION_MOVE){
      // Re-evaluate every active finger so steering + throttle/brake can be used together.
      for(int i=0;i<e.getPointerCount();i++){int pid=e.getPointerId(i);float px=e.getX(i),py=e.getY(i);
        if(pid==leftId||pid==rightId||pid==brakeId||pid==throttleId||pid==nitroId){
          clearPointer(pid); assignPointer(pid,px,py);
        }
      }
      return true;
    }
    if(a==MotionEvent.ACTION_UP||a==MotionEvent.ACTION_POINTER_UP||a==MotionEvent.ACTION_CANCEL){
      if(a==MotionEvent.ACTION_CANCEL){left=right=brake=throttle=nitro=false;leftId=rightId=brakeId=throttleId=nitroId=-1;}
      else clearPointer(id);
      return true;
    }
    return true;
  }
  void haptic(long ms){ try{ if(vibrator!=null && vibrator.hasVibrator()){ if(android.os.Build.VERSION.SDK_INT>=26) vibrator.vibrate(VibrationEffect.createOneShot(ms,VibrationEffect.DEFAULT_AMPLITUDE)); else vibrator.vibrate(ms); } }catch(Exception ignored){} }
  void reset(){missionType=(level-1)%3; missionReward=0; runTokens=0; combo=0; comboTimer=0; trailTokens=save.getInt("trailTokens",0); lowFuelWarned=false;edgeWarned=false;finishWarned=false;checkpointWarned=false;serviceWarned=false;servicePulse=0;sectionPulse=0;repairPulse=0;lastObstacleHit=-1;fuelPickupTaken=new boolean[]{false,false};repairPickupTaken=new boolean[]{false,false};bestDistance=save.getFloat("best_"+level,0);savedBestCheckpoint=bestDistance;carX=0;speed=0;distance=0;fuel=100;damage=0;checkpointPulse=0;steer=0;suspension=0;cameraShake=0;terrainBump=0;rutPhase=0;tractionFeedback=0;steeringVisual=0;nextCoinCheckpoint=250;introTimer=1.8f;paused=false;finished=false;failed=false;stopControls();last=System.nanoTime();save.edit().putInt("unlocked",unlocked).putFloat("coins",coins).putFloat("best_"+level,bestDistance).apply();}
}
