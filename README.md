# SpinaciCore
## Introduction
This project was an experiment in writing a server for an online video game. The academic goal was to get hands-on experience about designing and building a complex server from scratch, without missing any aspects of it: from basic blocks such as networking, cryptography and concurrency to managing the world itself.
This project started as a part of the [Ensimag](http://ensimag.grenoble-inp.fr/welcome/)’s second year module ‘Specialty project’ and is still under development. The idea was at the initiative of the students.
The source code under MIT license is available at https://github.com/SKNZ/SpinaciCore.
## World of Warcraft
For such a purpose, we chose the [World of Warcraft](https://en.wikipedia.org/wiki/World_of_Warcraft) video game. Indeed, writing our own video game client would have been both out of the scope of this project and, in terms of time spent, mutually exclusive with writing the server for it. Moreover, the video game sector has by nature little to no available open source game clients that would fit the purpose of this project. Consequently, it was decided to settle on an existing video game.
With prior knowledge and additional research, World of Warcraft was determined to be the video game for which writing a server would be most interesting: being the most popular and populous game of its type for the last decade, the technical aspects were certain to be production grade. Furthermore, the protocol is well documented and there exists very advanced open source implementations of it.
### Disclaimer
World of Warcraft is proprietary software. Regular players pay a monthly subscription fee. This project is only done as a learning experience, by students in a fully academic context. In other words, this is a research project with no goals whatsoever towards facilitating copyright infringement.
This project is under the MIT license, as found in the repository’s root folder.
## General information about MMORPGs
MMORPGs are exclusively online video games: without a network connection, the game cannot be played. Unlike more traditional games such as first-person shooters, the players evolve in a fully shared persistent open world.
In these games, the server is authoritative: in real time, each player tells of its actions to the server, which authorizes them and then sends out the information to the other players (for example, players will be informed when a nearby players attack a creature). In terms of network topology, this model is known as the star model, in which every communication goes through a central server.
On a non-technical note, for the players of such game, the goals are often about creating a character and making it stronger, e.g. by fighting creatures and gaining equipment.

## Stated objectives
This project was done as a part of the Ensimag’s module named ‘Specialty project’. With World of Warcraft’s development budget numbering in millions of dollars and a single semester of classes at our disposal, it was obvious that only a minimal subset of features from the original game server could be implemented.
The features to be implemented are:
* Authentication
* Realm selection
* Character management
* Joining the world
* Movement
* Seeing other players actions
While all these features are extremely basic and are all-together insufficient for anyone to consider seriously playing the game, they did provide enough work to last until the end of the project.

## Future
While the previous features have already been developed, some aspects, such as database integration, still need to be fully completed before moving to the next challenges.

For more information, please check the [full report](https://ensiwiki.ensimag.fr/images/f/f3/Light_report.pdf)

