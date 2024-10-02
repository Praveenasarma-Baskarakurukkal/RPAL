# Detect the operating system
ifeq ($(OS),Windows_NT)
	RM = del /q
	RM_FOLDERS = rmdir /s /q
	SEP = \\
else
	RM = rm -f
	RM_FOLDERS = rm -rf
	SEP = /
endif


# Compiler
JAVAC = javac

# Java source file
JAVA_SRC = myrpal.java

# Compiled Java class
JAVA_CLASS = myrpal.class

# Directory
DIRECTORY = Group_99

# Target to compile Java file
compile:
	cd $(DIRECTORY) && $(JAVAC) $(JAVA_SRC)

# Target to clean up compiled files
clean:
	cd $(DIRECTORY) && rm -f $(JAVA_CLASS)



