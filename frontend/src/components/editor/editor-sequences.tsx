import { Sequence } from 'omp-schema';
import * as React from 'react';

import { FocusableTextField } from "../utils/textfield";

import { safe } from '.';

export interface SequencesEditorProps {
    currentItem: Sequence;
    disableEditing: boolean;
}
export interface SequencesEditorDispatch {
    nameChanged: (id: string, name: string) => void;
}

const SequencesEditor = (props: SequencesEditorProps & SequencesEditorDispatch) => (
    <FocusableTextField
        label="Sequence Name"
        disabled={props.disableEditing}
        fullWidth={true}
        onChange={(ev: any) => props.nameChanged(safe(props.currentItem, it => it.id), ev.target.value)}
        value={safe(props.currentItem, it => it.name)}
        focusId='sequencename'
    />
);

export default SequencesEditor;
