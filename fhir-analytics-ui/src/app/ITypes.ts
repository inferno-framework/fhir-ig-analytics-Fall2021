import { IIg} from "./IIg";
import { IAsdtypes} from "./IAsdtypes";

export interface ITypes {
    status: string;
    identifier: string;
    csTypes: IIg;
    accumulatedSdTypes: IAsdtypes;
}