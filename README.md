# MDM Offline: Desktop console

## The platform

**MDM Offline** is a lightweight, private mobile device management system that stays entirely on your local network. There is no cloud account, and management data does not leave your office, home, or team network. Devices do not need to be enrolled as managed devices, and no managed profiles are required.

It is made of two applications that work together:

- **Desktop console** (this project): the control room on a PC
- **Device client**: the companion app on each phone or tablet you want to oversee

Put the PC and the devices on the same Wi-Fi or office network, keep the console running, and enrolled devices appear on the PC. From there you can see which devices are online, inspect their status, and keep them under local watch.

The product is designed for small fleets: an office, a household, or a shared set of phones and tablets where tighter control matters and a public cloud MDM is more than you want.

The interface is available in English and Italian.

## This project

This repository is the **desktop console**: the PC application that operators use to watch and manage devices.

On first launch it walks you through how the platform stays local and how to pair the mobile app. After that it becomes a live dashboard of enrolled phones and tablets: who is online, when they last checked in, and the identity and facts each device has reported.

Closing the window does not stop protection. The console keeps running from the system tray so devices can still check in. You leave from the tray only when you want the server to stop.

To bring a device under watch, install the MDM Offline client on that phone or tablet and open it on the same network as this PC.
