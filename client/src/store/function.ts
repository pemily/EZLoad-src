const idStartTimestamp = 1563902430151;

let previousId: number;

export function idGenerator(): string {
    if (previousId === undefined) {
        const timestamp = (+new Date()) - idStartTimestamp;
        previousId = timestamp;
    }
    previousId = ++previousId;

    return previousId as unknown as string;
}

// prend une date iso (retourner par le composant Calendar)
// et la rend sous forme: dd/mm/yyyy (il y aura un 0 devant le jour et le mois si < 10)
export function date2str(isoDate: string) {
    const d: Date = new Date(isoDate);
    const month: number = d.getMonth() + 1;
    const day: number = d.getDate();
    const newDate = (day < 10 ? "0" + day : day)
                + "/" + (month < 10 ? "0" + month : month )
                + "/" + d.getFullYear();
    return newDate;
}

// the input must be 23/01/2021
export function str2date(dateStr: string): Date {
    const dateParts = dateStr.split("/");
    // month is 0-based, that's why we need dataParts[1] - 1
    const dateObject: Date = new Date(+parseInt(dateParts[2], 10),
        parseInt(dateParts[1], 10) - 1, +parseInt(dateParts[0], 10));
    return dateObject;
}

// return true if date1 is before date2
export function isBefore(date1: Date, date2: Date) {
    return date1.getTime() < date2.getTime();
}

export function removeUndefined<T>(obj: T): T {           // remove undefined attribute if exists
    const newObj: any = JSON.parse(JSON.stringify(obj));
    return newObj;
}
export function removeUndefinedArray<T>(obj: Array<T | undefined>): T[] {           // remove undefined attribute if exists
    const newObj: any = JSON.parse(JSON.stringify(obj));
    return newObj;
}


export function generateNameFromEmail(email: string) {
    const index = email.indexOf("@");
    if (index === -1) { return email; }
    return email.substr(0, index);
}


export function checkEmailFormat(email: string) {
    const re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(email);
}