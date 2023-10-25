#!/bin/bash

# Define the directory where you want to clone the repository
REPO_DIR=/srv/MSI_CLOUD

# GitHub repository URL
REPO_URL="https://github.com/MartinLei/MSI_CLOUD.git"

# Check if the repo directory exists. If not, clone the repository.
if [ ! -d "$REPO_DIR" ]; then
    echo "Cloning the GitHub repository..."
    git clone "$REPO_URL" "$REPO_DIR"
else
    echo "Repository already exists. Skipping cloning."
fi

# Change to the repository directory
cd "$REPO_DIR"

# Pull the latest changes from the repository
git pull

echo $(date -u)  "Start compose build"
/usr/bin/docker compose build

echo $(date -u)  "Start compose up"
/usr/bin/docker compose up -d
echo $(date -u)  "Done"

