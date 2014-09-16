SimpleDht
=========

Simple Distributed Hash Table based on Chord


- This is a simplified version of Chord; there are three things which have been implemented:
    1. ID space partitioning/re-partitioning
    2. Ring-based routing
    3. Node joins.

- Not implemented :
    1. finger tables and finger-based routing
    2. Node leaves/failures 
    3. Concurrent node joins 

- If you run multiple instances of this  app, all content provider instances form a Chord ring and serve insert/query requests in a distributed fashion according to the Chord protocol.
- SHA-1 is used as hash function to generate keys.
- port numbers (see below) are fixed. This means that we can use the port numbers (11108, 11112, 11116, 11120, & 11124) as successor and predecessor pointers.
- not needed to handle insert/query requests while a node is joining. Assumed that insert/query requests will be issued only with a stable system.

- Main Activity :
   LDump :
    When touched, this button should dump and display all the <key, value> pairs stored in your local partition of the node.
    This means that this button can give “@” as the selection parameter to query().

    GDump:
    When touched, this button should dump and display all the <key, value> pairs stored in your whole DHT. Thus, LDump button is for local dump, and this button (GDump) is for global dump of the entire <key, value> pairs.
    This means that this button can give “*” as the selection parameter to query().
