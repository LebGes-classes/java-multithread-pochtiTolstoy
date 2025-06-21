# Makefile for Java Multithreading Project

.PHONY: build run clean generate

# Default target
all: build

# Build the project
build:
	@echo "Building project..."
	mvn -q compile

# Run the application (only builds if target doesn't exist)
run:
	@if [ ! -d "target/classes" ]; then \
		echo "Build not found, building first..."; \
		mvn -q compile; \
	fi
	@echo "Running application..."
	mvn -q exec:java -Dexec.mainClass="com.example.multithreading.Main"

# Generate initial Excel data
generate:
	@echo "Generating initial Excel data..."
	mvn -q compile exec:java -Dexec.mainClass="com.example.multithreading.ExcelGenerator"

# Clean build artifacts
clean:
	@echo "Cleaning project..."
	mvn -q clean

rebuild: clean build

# Help target
help:
	@echo "Available targets:"
	@echo "  build     - Compile the project"
	@echo "  run       - Run the application (builds only if needed)"
	@echo "  generate  - Generate initial Excel data (work_data.xlsx)"
	@echo "  clean     - Clean build artifacts"
	@echo "  help      - Show this help message" 