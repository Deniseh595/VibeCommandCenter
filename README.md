# VibeCommandCenter

Android App for A/V control 

Structure: 
Menu activity --> 2 main control files (LEDControl.java and Chameleon.java)

BT commands sent defined at top of control files 

LEDControl
- LED on/off switch - "ledon", "ledoff"
- Brightness control buttons - "brighten","dim"
- Cloud on/off switch - "cloudon","cloudoff"
- New choices are "cloud1" - "cloud8"

Chameleon
- Button 0,0 = "choiceone"
- Button 0,1 = "choicetwo"
- Button 1,0 = "choicethree"
... "choiceeight"
- Cancel button = "choiceoff"
