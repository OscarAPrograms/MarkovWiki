# MarkovWiki
MarkovWiki stochastically generates (often nonsensical) text about any topic of the user's choosing.

After the user searches for an existing, valid Wikipedia article using the program's GUI (made in JavaFX to resemble Wikipedia's website), text from that article (accessed through [jwiki](https://github.com/fastily/jwiki)) is used to build a Markov chain model. The resulting model randomly generates two initial words and then repeatedly generates new words based on the two most recently generated words until 5 sentences are written. This text is finally presented to the screen via the GUI.

## Note:
This program must be run with an internet connection (otherwise Wikipedia cannot be accessed).
