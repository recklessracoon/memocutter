# MemoCutter

MemoCutter is a fast and easy way to cut out unwanted sound parts from voice messages received via common instant messaging services and resend the result. 

# Features

-AppIntro: Short manual on how to use
-overview over edited audios
-rename an audio
-delete an audio
-replay audios
-share audio via any app that supports it
-customize the app with background styles

Fully supported audio formats are .opus and .mp3

For a more complete manual than that in the AppIntro, view Readme:
https://github.com/recklessracoon/memocutter

# Manual

# Main
![alt text](https://i.imgur.com/lgfItw5.png)

Settings button top right
- Activate/Deactivate AppIntro
- Activate/Deactivate Text Message after changing background
- Change background "Style" to a preset style
- Or a custom one (lets you choose a picture from gallery)
- View about page with version info, contact and credits

Search folders
- Lets you browse your files with system filebrowser

Last cut audio files
- Opens another activity, see "Overview"

Cut audios together
- Opens another activity, see "Overview 2"

# Overview
![alt text](https://i.imgur.com/c5JDWpP.png)

- Gives an overview over all edited audios
- Search by name possible
- Replay audio by clicking the respective play button
- Long click on item to rename it
- Click on Item to open new activity, see "Edit"
- Swipe left to delete

# Overview 2
![alt text](https://i.imgur.com/Q342qV1.png)

- Gives an overview over all edited audios
- Search by name possible
- Replay audio by clicking the respective play button
- Long click on item to mark it
- Buttom right button to combine the marked files
- Swipe left to delete

# Edit
![alt text](https://i.imgur.com/ac0Mtaq.png)

- Lets you replay the audio by pressing play button
- Top right share button
  -> Lets you share the selected audio via another app
- Use rangebar to mark the area you want to cut out
- TextViews show the exact milliseconds of the beginning and end
- Press bottom right button to confirm
  -> New audio will pop up below
  - Long click to rename
  - Swipe left to delete
  - Share button to share this audio via another app

# Credits

Gold, Pink, Blue backgrounds designed by kjpargeter / Freepik
http://www.freepik.com

Material design icons provided by Android Studio's Image Asset Studio

# Used libraries

- Wrapper for ffmpeg, a linux command line tool to edit audio and video files
https://github.com/WritingMinds/ffmpeg-android-java

- Easy way to include an AppIntro in your android project
https://github.com/apl-devs/AppIntro

- Easy way to include an about page in your android project
https://github.com/medyo/android-about-page

- Customizable RangeBar for android
https://github.com/recklessracoon/simplerangebar-android
