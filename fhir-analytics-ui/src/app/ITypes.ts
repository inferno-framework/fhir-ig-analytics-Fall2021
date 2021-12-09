import { IAsdtypes} from "./IAsdtypes";
import { IAcstypes} from "./IAcstypes";

export interface ITypes {
    status: string;
    identifier: string;
    accumulatedSdTypes: IAsdtypes;
    accumulatedCsTypes: IAcstypes;
}