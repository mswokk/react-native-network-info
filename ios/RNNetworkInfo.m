//
//  RNNetworkInfo.m
//  RNNetworkInfo
//
//  Created by Corey Wilson on 7/12/15.
//  Copyright (c) 2015 eastcodes. All rights reserved.
//

#import "RNNetworkInfo.h"

#import <ifaddrs.h>
#import <arpa/inet.h>

@import SystemConfiguration.CaptiveNetwork;

@implementation RNNetworkInfo

RCT_EXPORT_MODULE();

+ (NSDictionary *) getNetworkInterfaceInfo {
    NSArray *interfaceNames = CFBridgingRelease(CNCopySupportedInterfaces());
    NSLog(@"%s: Supported interfaces: %@", __func__, interfaceNames);
    
    NSDictionary *SSIDInfo;
    for (NSString *interfaceName in interfaceNames) {
        SSIDInfo = CFBridgingRelease(CNCopyCurrentNetworkInfo((__bridge CFStringRef)interfaceName));
        if (SSIDInfo.count > 0) {
            return SSIDInfo;
        }
    }
    
    return nil;
}

RCT_REMAP_METHOD(getSSID,
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)
{
    id netInfo = [RNNetworkInfo getNetworkInterfaceInfo];
    id SSID = @"NoConnection";
    if(netInfo != nil){
        SSID = netInfo[@"SSID"];
    }
    resolve(SSID);
}


RCT_REMAP_METHOD(getBSSID,
                 getBSSIDResolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)
{

    id netInfo = [RNNetworkInfo getNetworkInterfaceInfo];
    id BSSID = @"NoConnection";
    if(netInfo != nil){
        BSSID = netInfo[@"BSSID"];
    }
    resolve(BSSID);
}


RCT_REMAP_METHOD(getIPAddress,
                 resolver2:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)
{
    NSString *address = @"error";
    
    struct ifaddrs *interfaces = NULL;
    struct ifaddrs *temp_addr = NULL;
    int success = 0;
    
    success = getifaddrs(&interfaces);
    
    if (success == 0) {
        temp_addr = interfaces;
        while(temp_addr != NULL) {
            if(temp_addr->ifa_addr->sa_family == AF_INET) {
                if([[NSString stringWithUTF8String:temp_addr->ifa_name] isEqualToString:@"en0"]) {
                    address = [NSString stringWithUTF8String:inet_ntoa(((struct sockaddr_in *)temp_addr->ifa_addr)->sin_addr)];
                }
            }
            temp_addr = temp_addr->ifa_next;
        }
    }
    
    freeifaddrs(interfaces);
    resolve(@[address]);
}

@end
