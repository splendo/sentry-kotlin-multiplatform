#!/usr/bin/env bash

if ! gem list cocoapods-generate -i > /dev/null 2>&1; then
  echo "Gem cocoapods-generate is not installed!"
  echo "This gem needs to be installed with sudo"
  sudo bundle config build.ffi --enable-libffi-alloc
  sudo bundle install
fi

echo "Gem cocoapods-generate is installed!"
