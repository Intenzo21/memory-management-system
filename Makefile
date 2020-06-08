
RM_FLAGS="-f"


messenger:
	javac MessageServer.java; \
	javac MessageClient.java; \
	javac MessageReadThread.java; \
	javac MessageWriteThread.java; \
	javac ClientConnection.java

messengerclean:
	rm $(RM_FLAGS) *.class *~; \


