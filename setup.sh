#!/bin/sh -e

cp ./git/hooks/* ./.git/hooks/
git config --global alias.st status
git config --global alias.staus status

