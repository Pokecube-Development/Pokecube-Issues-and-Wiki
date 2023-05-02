import json

test_file = './deoxys-normal.bbmodel'

obj = json.load(open(test_file,'r'))

animations = obj['animations']

def empty_arr(arr):
    return arr[0] == 0 and arr[1] == arr[0] and arr[2] == arr [1]

class xml_animation_segment:
    def __init__(self, length, start_time):
        self.posOffset = [0,0,0]
        self.rotOffset = [0,0,0]
        self.scaleOffset = [0,0,0]
        self.length = int(length) # casted to ints for ticks
        self.name = '??' # Component name is no longer used, but kept for legacy reasons
        self.startKey = int(start_time) # also casted to ints for ticks
         
        self.posChange = [0,0,0]
        self.rotChange = [0,0,0]
        self.scaleChange = [0,0,0]

        self.hidden = False

        self.posFuncs = ''
        self.rotFuncs = ''
        self.scaleFuncs = ''

    def print(self):
        xml = f'<component length="{self.length}" name="{self.name}" startKey="{self.startKey}"'
        
        if self.hidden:
            xml = xml + ' hidden="true"'
        else:
            if self.posOffset != None:
                xml = xml + f' posOffset="{self.posOffset[0]:.3f},{self.posOffset[1]:.3f},{self.posOffset[2]:.3f}"'
            if self.rotOffset != None:
                xml = xml + f' rotOffset="{self.rotOffset[0]:.3f},{self.rotOffset[1]:.3f},{self.rotOffset[2]:.3f}"'
            if self.scaleOffset != None:
                xml = xml + f' scaleOffset="{self.scaleOffset[0]:.3f},{self.scaleOffset[1]:.3f},{self.scaleOffset[2]:.3f}"'

            if self.posChange != None:
                xml = xml + f' posChange="{self.posChange[0]:.3f},{self.posChange[1]:.3f},{self.posChange[2]:.3f}"'
            if self.rotChange != None:
                xml = xml + f' rotChange="{self.rotChange[0]:.3f},{self.rotChange[1]:.3f},{self.rotChange[2]:.3f}"'
            if self.scaleChange != None:
                xml = xml + f' scaleChange="{self.scaleChange[0]:.3f},{self.scaleChange[1]:.3f},{self.scaleChange[2]:.3f}"'

        xml = xml + '/>'
        return xml
    
    def __repr__(self) -> str:
        return self.print()


class animation_segment:
    def __init__(self, time) -> None:
        self.rotations = [0,0,0]
        self.positions = [0,0,0]
        self.scales = [0,0,0]
        self.has_scale = False
        self.is_bedrock = False
        self.time = int(time * 20) # * 20 to convert to ticks

    def process(self, keyframe):
        channel = keyframe["channel"]
        points = keyframe["data_points"]
        data = points[0]
        x = data['x']
        y = data['y']
        z = data['z']

        # Try to convert to floats, later we check if not a float
        # for handling molang stuff
        try:
            x = float(x)
        except:
            pass
        try:
            y = float(y)
        except:
            pass
        try:
            z = float(z)
        except:
            pass

        if channel == 'rotation':
            self.rotations[0] = x
            self.rotations[1] = y
            self.rotations[2] = z
        elif channel == 'position':
            self.positions[0] = x
            self.positions[1] = y
            self.positions[2] = z
        elif channel == 'scale':
            self.has_scale = True
            self.scales[0] = x
            self.scales[1] = y
            self.scales[2] = z

    def set_doubles(self, _to, _from):
        all_valid = True
        for i in range(3):
            if(isinstance(_from[i], float)):
                _to[i] = _from[i]
            else:
                all_valid = False
        return all_valid
    
    def set_diff(self, _arr, _pos, _neg):
        all_valid = True
        for i in range(3):
            if(isinstance(_pos[i], float) and isinstance(_neg[i], float)):
                _arr[i] = _pos[i] - _neg[i]
            else:
                all_valid = False
        return all_valid

    def to_xml(self, first_frame, next_frame):

        startKey = self.time
        length = next_frame.time - self.time

        segment = xml_animation_segment(length, startKey)

        all_not_func = True
        if first_frame == self:
            all_not_func = self.set_doubles(segment.rotOffset, self.rotations) & all_not_func
            # all_not_func = self.set_doubles(segment.posOffset, self.positions) & all_not_func
            all_not_func = self.set_doubles(segment.scaleOffset, self.scales) & all_not_func

            # These coordinates are flipped for some reason.
            old = [x for x in segment.posOffset]
            segment.posOffset[0] = -old[0] * 1 / 16
            segment.posOffset[1] = -old[2] * 1 / 16
            segment.posOffset[2] = +old[1] * 1 / 16

            if self.is_bedrock:
                segment.posOffset[0] *= -1

            segment.rotOffset[0] = -segment.rotOffset[0]
            segment.rotOffset[1] = -segment.rotOffset[1]

            if (self.has_scale):
                segment.scaleOffset[0] = segment.scaleOffset[0]
                segment.scaleOffset[1] = segment.scaleOffset[1]
                segment.scaleOffset[2] = segment.scaleOffset[2]
                if (segment.scaleOffset[0] <= 0): 
                    segment.hidden = True
            

        if next_frame != first_frame:
            all_not_func = self.set_diff(segment.rotChange, self.rotations, next_frame.rotations) & all_not_func
            all_not_func = self.set_diff(segment.posChange, self.positions, next_frame.positions) & all_not_func
            all_not_func = self.set_diff(segment.scaleChange, self.scales, next_frame.scales) & all_not_func

            segment.rotChange[0] = -segment.rotChange[0]
            segment.rotChange[1] = -segment.rotChange[1]

            old = [x for x in segment.posChange]
            segment.posChange[0] = -old[0] * 1 / 16
            segment.posChange[1] = -old[2] * 1 / 16
            segment.posChange[2] = +old[1] * 1 / 16

            if (self.has_scale):
                segment.scaleChange[0] = segment.scaleChange[0]
                segment.scaleChange[1] = segment.scaleChange[1]
                segment.scaleChange[2] = segment.scaleChange[2]
                if (segment.scaleChange[0] <= 0): 
                    segment.hidden = True

        if empty_arr(segment.rotOffset):
            segment.rotOffset = None
        if empty_arr(segment.posOffset):
            segment.posOffset = None
        if not self.has_scale:
            segment.scaleOffset = None
        if empty_arr(segment.rotChange):
            segment.rotChange = None
        if empty_arr(segment.posChange):
            segment.posChange = None
        if not self.has_scale:
            segment.scaleChange = None

        return segment

converted = {}
for animation in animations:
    animators = animation['animators']
    parts = {}
    converted[animation['name']] = parts
    for key, part in animators.items():
        key = part['name']
        segments = {}
        for keyframe in part['keyframes']:
            time = float(keyframe['time'])
            if time in segments:
                frame = segments[time]
            else:
                frame = animation_segment(time)
                segments[time] = frame
            frame.process(keyframe)

        frames = [x for x in segments.values()]
        frames.sort(key=lambda x: x.time)

        xml_frames = []
        if len(frames) == 1:
            xml_frames.append(frames[0].to_xml(frames[0], frames[0]))
        else:
            first_frame = frames[0]
            for i in range(0, len(frames)-1):
                next_frame = frames[i + 1]
                frame = frames[i]
                xml_frames.append(frame.to_xml(first_frame, next_frame))
        parts[key] = xml_frames

file = open('./' + test_file.replace('bbmodel', 'xml'), 'w')
for name, parts in converted.items():
    file.write(f' <phase type="converted_{name}">\n')
    for part_name, frames in parts.items():
        file.write(f'  <part name="{part_name}">\n')
        for frame in frames:
            file.write(f'   {frame}\n')
        file.write(f'  </part>\n')
    file.write(f' </phase>\n')
