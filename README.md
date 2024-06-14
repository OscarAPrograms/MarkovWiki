# Markov-Wiki
Markov-Wiki stochastically generates (often nonsensical) text about any topic of the user's choosing.

After the user searches for a Wikipedia article using the program's GUI, text from that article (accessed through [jwiki](https://github.com/fastily/jwiki)) is used to build a Markov chain model. The resulting model randomly generates three initial words and then repeatedly generates new words based on the three most recently generated words until 10 sentences are written. This text is finally presented to the screen via the GUI (which is made to resemble Wikipedia's website).
