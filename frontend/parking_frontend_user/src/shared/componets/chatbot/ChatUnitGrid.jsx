import React from 'react';
import './ChatUnitGrid.css';

const ChatUnitGrid = ({ units, onSelect }) => {
    if (!units || units.length === 0) return null;

    return (
        <div className="cug-wrap">
            {units.map((unitNo) => (
                <button
                    key={unitNo}
                    className="cug-chip"
                    onClick={() => onSelect(`${unitNo}호`)}
                >
                    {unitNo}호
                </button>
            ))}
        </div>
    );
};

export default ChatUnitGrid;
