const base = {
    display: 'flex',
    alignItems: 'center',
};
export const column = {
    ...base,
    flexFlow: 'column nowrap',
};
export const row = {
    ...base,
    flexFlow: 'row nowrap',
};
export const hidden = {
    display: 'none !important',
};
