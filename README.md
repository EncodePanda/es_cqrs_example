# es_cqrs_example_in_akka

Note: this is only quick description of the project. The project is still in early shape of development. And so is its
documentation :)


Sample application showing how Event Sourcing + CQRS can be implemented in Akka. This code is constantly improving since
I try to find a state-of-the-art approach to the problem. The idea is to test all possible scenarios and corner cases for
ES+CQRS architecture, showing both power and weaknesses of this design.

## Design decisions

### approach 1

At the moment I am finishing first approach. The code is based on activator template created by people from Scalac
(kudos to @lukasgasior). This approach is however to tightly coupled with Akka. Once I'm done with creating basic commands &
 queries for propsed domain problem (see below) I will move to the second approach.

### approach 2

This is still working progress, waiting till approach 1 is finished. Idea that I currently have in mind is to
make akka-persistence only an pluggable infrastructure, decoupling it from ES+CQRS problem.

## Domain

When I've started developing this project I was visiting Gdansk (Poland), where my wife was attending a huge conference
for blogging community. As I was looking for a domain problem for this project, bloggers were as good as any other,
but they were really appealing since they constantly interact with each other and application that could trace the history
of such events seemed ok.