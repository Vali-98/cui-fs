
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNCuiFsSpec.h"

@interface CuiFs : NSObject <NativeCuiFsSpec>
#else
#import <React/RCTBridgeModule.h>

@interface CuiFs : NSObject <RCTBridgeModule>
#endif

@end
